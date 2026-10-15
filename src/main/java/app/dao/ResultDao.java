package app.dao;

import app.model.Course;
import app.model.ResultEntry;
import app.model.Student;

import java.sql.SQLException;
import java.util.List;

public interface ResultDao {
    List<Student> findAllStudents() throws SQLException;
    List<Course> findAllCourses() throws SQLException;
    List<ResultEntry> findResultsByStudentAndSession(int studentId, String session) throws SQLException;
    void saveResult(int studentId, int courseId, String session, double score, String grade, int gradePoint) throws SQLException;
}
