package app.model;

public class ResultEntry {
    private String courseCode;
    private String courseTitle;
    private int creditUnit;
    private double score;
    private String grade;
    private int gradePoint;

    public ResultEntry(String courseCode, String courseTitle, int creditUnit, double score, String grade, int gradePoint) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.creditUnit = creditUnit;
        this.score = score;
        this.grade = grade;
        this.gradePoint = gradePoint;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public int getCreditUnit() { return creditUnit; }
    public double getScore() { return score; }
    public String getGrade() { return grade; }
    public int getGradePoint() { return gradePoint; }
    public int getWeightedPoint() { return creditUnit * gradePoint; }
}
