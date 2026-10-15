package app.dao;

import app.model.Course;
import app.model.ResultEntry;
import app.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteResultDao implements ResultDao {

    @Override
    public List<Student> findAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT id, matric_no, full_name, department, level FROM students ORDER BY matric_no ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Student(
                    rs.getInt("id"),
                    rs.getString("matric_no"),
                    rs.getString("full_name"),
                    rs.getString("department"),
                    rs.getInt("level")
                ));
            }
        }
        return list;
    }

    @Override
    public List<Course> findAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT id, code, title, credit_unit, semester FROM courses ORDER BY code ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Course(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("title"),
                    rs.getInt("credit_unit"),
                    rs.getInt("semester")
                ));
            }
        }
        return list;
    }

    @Override
    public List<ResultEntry> findResultsByStudentAndSession(int studentId, String session) throws SQLException {
        List<ResultEntry> list = new ArrayList<>();
        String sql = """
            SELECT c.code, c.title, c.credit_unit, r.score, r.grade, r.grade_point
            FROM results r
            JOIN courses c ON r.course_id = c.id
            WHERE r.student_id = ? AND r.session = ?
            ORDER BY c.code ASC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, session);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ResultEntry(
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credit_unit"),
                        rs.getDouble("score"),
                        rs.getString("grade"),
                        rs.getInt("grade_point")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public void saveResult(int studentId, int courseId, String session, double score, String grade, int gradePoint) throws SQLException {
        String sql = """
            INSERT INTO results (student_id, course_id, session, score, grade, grade_point)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(student_id, course_id, session) DO UPDATE SET
                score = excluded.score,
                grade = excluded.grade,
                grade_point = excluded.grade_point
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.setString(3, session);
            stmt.setDouble(4, score);
            stmt.setString(5, grade);
            stmt.setInt(6, gradePoint);
            stmt.executeUpdate();
        }
    }
}
