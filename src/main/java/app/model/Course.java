package app.model;

public class Course {
    private int id;
    private String code;
    private String title;
    private int creditUnit;
    private int semester;

    public Course(int id, String code, String title, int creditUnit, int semester) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.creditUnit = creditUnit;
        this.semester = semester;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCreditUnit() { return creditUnit; }
    public int getSemester() { return semester; }
}
