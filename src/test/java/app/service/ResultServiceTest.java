package app.service;

import app.dao.DatabaseConnection;
import app.dao.DatabaseInitializer;
import app.dao.ResultDao;
import app.dao.SqliteResultDao;
import app.model.ResultEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResultServiceTest {

    private ResultService service;
    private SqliteResultDao dao;

    @BeforeEach
    void setUp() {
        DatabaseInitializer.initialize();
        dao = new SqliteResultDao();
        service = new ResultService(dao);

        // Clean results table before each test to maintain test isolation
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM results;");
        } catch (SQLException e) {
            fail("Failed to reset test database: " + e.getMessage());
        }
    }

    @ParameterizedTest(name = "Score {0} should map to Grade {1} and Point {2}")
    @CsvSource({
        "100.0, A, 5",
        "70.0,  A, 5",
        "69.9,  B, 4",
        "60.0,  B, 4",
        "59.9,  C, 3",
        "50.0,  C, 3",
        "49.9,  D, 2",
        "45.0,  D, 2",
        "44.9,  E, 1",
        "40.0,  E, 1",
        "39.9,  F, 0",
        "0.0,   F, 0"
    })
    @DisplayName("Boundary tests for 5-point grading scale")
    void testComputeGrade_boundaries(double score, String expectedGrade, int expectedPoint) {
        ResultService.GradeInfo info = service.computeGrade(score);
        assertEquals(expectedGrade, info.grade());
        assertEquals(expectedPoint, info.gradePoint());
    }

    @Test
    @DisplayName("Score out of range (< 0 or > 100) should throw IllegalArgumentException")
    void testComputeGrade_invalidScores() {
        assertThrows(IllegalArgumentException.class, () -> service.computeGrade(-1.0));
        assertThrows(IllegalArgumentException.class, () -> service.computeGrade(100.1));
    }

    @Test
    @DisplayName("GPA calculation with mixed grades and credit units")
    void testCalculateGPA_standard() {
        List<ResultEntry> results = List.of(
            new ResultEntry("CSC501", "AI", 3, 75.0, "A", 5),
            new ResultEntry("CSC503", "Software Eng", 3, 65.0, "B", 4),
            new ResultEntry("CSC505", "Database", 2, 55.0, "C", 3)
        );

        double gpa = service.calculateGPA(results);
        assertEquals(4.13, gpa, 0.01);
    }

    @Test
    @DisplayName("GPA calculation with empty list should return 0.0")
    void testCalculateGPA_empty() {
        assertEquals(0.0, service.calculateGPA(List.of()));
        assertEquals(0.0, service.calculateGPA(null));
    }

    @Test
    @DisplayName("Transaction Test: Batch result upload successfully commits all records")
    void testBatchResultUpload_commitsSuccessfully() throws SQLException {
        int studentId = 1;
        String session = "2025/2026";

        List<ResultService.ScoreSubmission> submissions = List.of(
            new ResultService.ScoreSubmission(1, 80.0), // CSC501 -> A
            new ResultService.ScoreSubmission(2, 65.0), // CSC503 -> B
            new ResultService.ScoreSubmission(3, 55.0)  // CSC505 -> C
        );

        service.recordSemesterResultsBatch(studentId, session, submissions);

        List<ResultEntry> entries = service.getStudentResults(studentId, session);
        assertEquals(3, entries.size(), "All 3 records should be committed atomically.");
    }

    @Test
    @DisplayName("Transaction Test: Failure on any record rolls back entire batch")
    void testBatchResultUpload_rollsBackOnFailure() {
        int studentId = 1;
        String session = "2025/2026";

        // Item 1 is valid (course_id 1 exists)
        // Item 2 has invalid course_id 9999 (violates FOREIGN KEY constraint)
        List<ResultDao.BulkGradeItem> batchWithInvalidCourse = List.of(
            new ResultDao.BulkGradeItem(1, 85.0, "A", 5),
            new ResultDao.BulkGradeItem(9999, 70.0, "A", 5)
        );

        // Expect SQLException due to FOREIGN KEY violation on second item
        assertThrows(SQLException.class, () -> {
            dao.saveResultsBatch(studentId, session, batchWithInvalidCourse);
        });

        // Verify that the first item was NOT inserted (rollback verified)
        try {
            List<ResultEntry> entries = service.getStudentResults(studentId, session);
            assertEquals(0, entries.size(), "Earlier writes must be rolled back on batch failure.");
        } catch (SQLException e) {
            fail("Query failed: " + e.getMessage());
        }
    }
}
