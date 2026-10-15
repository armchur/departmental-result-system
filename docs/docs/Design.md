# System Design Document (SDD)
## Departmental Result Management System

**Course**: CSC 508 - Advanced Software Engineering  
**System**: Desktop Departmental Result Processing Application  
**Version**: 1.0  
**Author**: Armstrong Finchur  
**Date**: October 2026  

---

## 1. Architectural Design

The Departmental Result Management System is structured according to a strict **3-Tier Layered Architecture**. Each layer has a well-defined boundary and communicates only with adjacent layers through explicit interfaces and domain models.

```text
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   ResultView.fxml  ◄──►  ResultViewController  ◄──► CSS │
└───────────────────────────┬────────────────────────────┘
                            │ Calls Service API
                            ▼
┌────────────────────────────────────────────────────────┐
│                     Service Layer                      │
│       ResultService (Grading, GPA, Transactions)       │
└───────────────────────────┬────────────────────────────┘
                            │ Calls DAO Interface
                            ▼
┌────────────────────────────────────────────────────────┐
│                Data Access Layer (DAO)                 │
│      ResultDao (Interface)  ◄──  SqliteResultDao       │
│      DatabaseConnection     ◄──  DatabaseInitializer   │
└───────────────────────────┬────────────────────────────┘
                            │ JDBC Parameterized SQL
                            ▼
┌────────────────────────────────────────────────────────┐
│                   Persistence Store                    │
│             SQLite 3 Database Engine                   │
└────────────────────────────────────────────────────────┘



Layer Responsibilities & Isolation Guarantees







Presentation Layer (app.ui):





Built with JavaFX 21 using declarative FXML (ResultView.fxml) and stylesheet (style.css).


ResultViewController manages UI events, binds observable data to the TableView, and catches presentation-level input errors.


Isolation Rule: Zero SQL queries, database drivers, or business formulas reside in this layer.









Service Layer (app.service):





ResultService encapsulates all business logic:



5-point university grading conversion (A=5 down to F=0).


Validation of score boundaries (
0
≤
score
≤
100
0≤score≤100).


Real-time GPA calculation with rounding.


Coordination of multi-course batch submissions.






Isolation Rule: Has no dependencies on JavaFX UI classes (javafx.*). The service can be tested headlessly in pure JUnit 5 test suites.









Data Access Layer (app.dao):





ResultDao defines domain persistence contracts.


SqliteResultDao implements operations using JDBC PreparedStatement and explicit transaction controls.


DatabaseConnection manages connection pooling/lifecycle.


DatabaseInitializer loads DDL schema and seed data on startup.


Isolation Rule: Exposes domain records and entities to the caller without leaking raw ResultSet objects.










2. Class & Component Design


2.1 Domain Models (app.model)




Student:



id: int


matricNo: String


fullName: String


department: String






Course:



id: int


code: String


title: String


creditUnit: int






ResultEntry:



Represents an aggregate view model combining result, student, and course data:


courseCode: String, courseTitle: String, creditUnit: int, score: double, grade: String, gradePoint: int, weightedPoint: int








2.2 Core Interfaces and Classes


classDiagram
    class ResultDao {
        <<interface>>
        +findAllStudents() List~Student~
        +findAllCourses() List~Course~
        +findResultsByStudentAndSession(int, String) List~ResultEntry~
        +saveResult(int, int, String, double, String, int) void
        +saveResultsBatch(int, String, List~BulkGradeItem~) void
    }

    class SqliteResultDao {
        -String dbUrl
        +findAllStudents() List~Student~
        +findAllCourses() List~Course~
        +findResultsByStudentAndSession(int, String) List~ResultEntry~
        +saveResult(int, int, String, double, String, int) void
        +saveResultsBatch(int, String, List~BulkGradeItem~) void
    }

    class ResultService {
        -ResultDao resultDao
        +computeGrade(double) GradeInfo
        +calculateGPA(List~ResultEntry~) double
        +getStudents() List~Student~
        +getCourses() List~Course~
        +getStudentResults(int, String) List~ResultEntry~
        +recordScore(int, int, String, double) void
        +recordSemesterResultsBatch(int, String, List~ScoreSubmission~) void
    }

    class ResultViewController {
        -ResultService service
        -TableView resultsTable
        -ComboBox studentBox
        -ComboBox courseBox
        -TextField scoreField
        -Label gpaLabel
        +initialize() void
        +initData(ResultService) void
        +handleRecordScore() void
        -refreshResults() void
    }

    ResultDao <|.. SqliteResultDao : implements
    ResultService --> ResultDao : depends on
    ResultViewController --> ResultService : depends on





3. Database Schema & Normalization Analysis


3.1 Relational Schema Diagram (Mermaid ERD)


erDiagram
    STUDENTS ||--o{ RESULTS : "earns"
    COURSES ||--o{ RESULTS : "awarded_in"

    STUDENTS {
        INTEGER id PK "AUTOINCREMENT"
        TEXT matric_no UK "NOT NULL, UNIQUE"
        TEXT full_name "NOT NULL"
        TEXT department "NOT NULL"
    }

    COURSES {
        INTEGER id PK "AUTOINCREMENT"
        TEXT code UK "NOT NULL, UNIQUE"
        TEXT title "NOT NULL"
        INTEGER credit_unit "NOT NULL, CHECK(credit_unit > 0)"
    }

    RESULTS {
        INTEGER id PK "AUTOINCREMENT"
        INTEGER student_id FK "NOT NULL"
        INTEGER course_id FK "NOT NULL"
        TEXT session "NOT NULL"
        REAL score "NOT NULL, CHECK(score >= 0 AND score <= 100)"
        TEXT grade "NOT NULL"
        INTEGER grade_point "NOT NULL"
    }



3.2 Database Normalization Analysis (1NF to 3NF)



The database schema is designed to conform strictly to Third Normal Form (3NF) to eliminate redundancy and update anomalies:



First Normal Form (1NF)




Criteria: All attributes contain atomic values; every column has a unique name; a primary key uniquely identifies each tuple.


Verification:



In students, matric_no, full_name, and department are atomic single-valued attributes.


In courses, code, title, and credit_unit are atomic values.


In results, each row holds exactly one course score for a single student in a given academic session. There are no repeating groups or multivalued attributes.








Second Normal Form (2NF)




Criteria: Conforms to 1NF, and all non-key attributes are fully functionally dependent on the entire primary key (no partial functional dependencies).


Verification:



Synthetic surrogate keys (id integers) serve as the primary key for all tables.


In results, the natural composite candidate key is (student_id, course_id, session). Non-key attributes (score, grade, grade_point) depend entirely on the combination of the student, the course, and the academic session. A student's grade in a course cannot be determined from student_id alone or course_id alone. Hence, no partial functional dependency exists.








Third Normal Form (3NF)




Criteria: Conforms to 2NF, and no non-key attribute is transitively dependent on the primary key (no transitive dependencies: 
𝑋
→
𝑌
X→Y and 
𝑌
→
𝑍
Y→Z).


Verification:



Course attributes (title, credit_unit) are kept in the separate courses table rather than duplicated in results.


Student biographical details (full_name, department) are kept in students rather than duplicated in results.


The aggregate values such as Weighted Points (
𝐶
𝑈
×
𝐺
𝑃
CU×GP) and Cumulative GPA are not stored in the database table to prevent data inconsistency. Instead, they are computed dynamically by the service layer at query time.










4. Transaction Management Design



For multi-write operations (such as registering a semester's results across several courses simultaneously), database atomicity is critical.



Transaction Flow:




SqliteResultDao.saveResultsBatch requests a connection from DatabaseConnection.


The DAO disables automatic commits:

connection.setAutoCommit(false);





A parameterized INSERT OR REPLACE statement is prepared.


Each course grade item is added to the batch execution.


If all statements execute successfully without constraint violation:

connection.commit();





If an SQLException occurs (e.g., non-existent course ID or foreign key violation):

connection.rollback();


Zero partial records are saved, guaranteeing the ACID atomicity property.


Finally, connection.setAutoCommit(true) is restored.






5. Security & Safety Design




SQL Injection Defense: Every dynamic parameter (student_id, course_id, session, score, grade, grade_point) is bound using PreparedStatement.setInt(), setString(), or setDouble(). No raw SQL string interpolation exists anywhere in the codebase.


Domain Constraints: Database-level check constraints (CHECK(score >= 0 AND score <= 100) and CHECK(credit_unit > 0)) reinforce application-level validation rules.