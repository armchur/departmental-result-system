package app.model;

public class Student {
    private int id;
    private String matricNo;
    private String fullName;
    private String department;
    private int level;

    public Student(int id, String matricNo, String fullName, String department, int level) {
        this.id = id;
        this.matricNo = matricNo;
        this.fullName = fullName;
        this.department = department;
        this.level = level;
    }

    public Student(String matricNo, String fullName, String department, int level) {
        this(0, matricNo, fullName, department, level);
    }

    public int getId() { return id; }
    public String getMatricNo() { return matricNo; }
    public String getFullName() { return fullName; }
    public String getDepartment() { return department; }
    public int getLevel() { return level; }
}
