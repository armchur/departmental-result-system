package app.service;

import app.dao.ResultDao;
import app.model.Course;
import app.model.ResultEntry;
import app.model.Student;

import java.sql.SQLException;
import java.util.List;

public class ResultService {

    private final ResultDao resultDao;

    public ResultService(ResultDao resultDao) {
        this.resultDao = resultDao;
    }

    public record GradeInfo(String grade, int gradePoint) {}

    // Business rule: Standard Nigerian University 5-point grading scale
    public GradeInfo computeGrade(double score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100.");
        }
        if (score >= 70) return new GradeInfo("A", 5);
        if (score >= 60) return new GradeInfo("B", 4);
        if (score >= 50) return new GradeInfo("C", 3);
        if (score >= 45) return new GradeInfo("D", 2);
        if (score >= 40) return new GradeInfo("E", 1);
        return new GradeInfo("F", 0);
    }

    // Business rule: GPA calculation
    public double calculateGPA(List<ResultEntry> results) {
        if (results == null || results.isEmpty()) {
            return 0.0;
        }
        int totalUnits = 0;
        int totalWeightedPoints = 0;

        for (ResultEntry r : results) {
            totalUnits += r.getCreditUnit();
            totalWeightedPoints += r.getWeightedPoint();
        }

        if (totalUnits == 0) return 0.0;
        double gpa = (double) totalWeightedPoints / totalUnits;
        return Math.round(gpa * 100.0) / 100.0; // 2 decimal places
    }

    public List<Student> getStudents() throws SQLException {
        return resultDao.findAllStudents();
    }

    public List<Course> getCourses() throws SQLException {
        return resultDao.findAllCourses();
    }

    public List<ResultEntry> getStudentResults(int studentId, String session) throws SQLException {
        return resultDao.findResultsByStudentAndSession(studentId, session);
    }

    public void recordScore(int studentId, int courseId, String session, double score) throws SQLException {
        GradeInfo gradeInfo = computeGrade(score);
        resultDao.saveResult(studentId, courseId, session, score, gradeInfo.grade(), gradeInfo.gradePoint());
    }
}
