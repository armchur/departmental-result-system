# Software Requirements Specification (SRS)
## Departmental Result Management System

**Course**: CSC 508 - Advanced Software Engineering  
**System**: Desktop Departmental Result Processing Application  
**Version**: 1.0  
**Author**: Armstrong Finchur  
**Date**: October 2026  

---

## 1. Introduction

### 1.1 Purpose
This document provides the formal Software Requirements Specification (SRS) for the **Departmental Result Management System**. It specifies the functional and non-functional requirements, user interactions, business rules, and architectural constraints governing the software.

### 1.2 Scope
The system is a self-contained desktop application designed for university academic departments (such as Computer Science). It allows departmental examination officers and academic advisers to:
1. View registered students and departmental course offerings.
2. Record individual student course examination scores.
3. Batch-process semester grades within an atomic database transaction.
4. Calculate letter grades, grade points, and semester Grade Point Averages (GPA) in real-time according to the standard Nigerian university 5-point grading system.
5. Inspect detailed student result transcripts by academic session.

### 1.3 Definitions, Acronyms, and Abbreviations
- **GPA**: Grade Point Average (calculated on a 0.00 – 5.00 scale).
- **Credit Unit (CU)**: The weight assigned to an academic course.
- **Grade Point (GP)**: Quality point assigned to a letter grade (A=5, B=4, C=3, D=2, E=1, F=0).
- **Weighted Point (WP)**: The product of a course's Credit Unit and Grade Point ($WP = CU \times GP$).
- **DAO**: Data Access Object pattern separating business logic from direct database SQL queries.
- **ACID**: Atomicity, Consistency, Isolation, and Durability transaction guarantees.

---

## 2. Overall Description

### 2.1 Product Perspective
The application follows a strict 3-tier architecture:
- **Presentation Layer**: JavaFX 21 desktop interface with declarative FXML and CSS.
- **Business/Service Layer**: Java service orchestrating domain rules, grading logic, and transaction management.
- **Data Access Layer**: SQLite JDBC DAO utilizing 100% parameterized queries.

```text
[ Presentation: JavaFX FXML / CSS / Controller ]
                       │
                       ▼
         [ Service Layer: ResultService ]
                       │
                       ▼
     [ Data Access Layer: SqliteResultDao ]
                       │
                       ▼
        [ Persistence: SQLite Database ]

2.2 User Characteristics
Primary Actor: Departmental Examination Officer / Course Coordinator.
User Skills: Basic computer literacy, familiarity with departmental course codes, matriculation numbers, and academic grading policies.
2.3 General Constraints
Platform: Cross-platform desktop runtime (Windows, macOS, Linux) supporting Java 17+.
Database Engine: Embedded SQLite 3 (no remote database server required).
Architecture: Strict layering. No SQL or calculation logic permitted inside JavaFX controllers or UI views.
Security: Prevention of SQL injection through mandatory parameterized PreparedStatements.
3. Specific Requirements & Business Rules
3.1 Business Rules (Grading Scale & GPA Formula)
Rule 1: 5-Point Grading Scale
Scores between 0.0 and 100.0 map strictly to letter grades and points:

Score Range (%)	Letter Grade	Grade Point	Remark
70.0
≤
Score
≤
100.0
70.0≤Score≤100.0	A	5	Excellent
60.0
≤
Score
<
70.0
60.0≤Score<70.0	B	4	Very Good
50.0
≤
Score
<
60.0
50.0≤Score<60.0	C	3	Good
45.0
≤
Score
<
50.0
45.0≤Score<50.0	D	2	Fair
40.0
≤
Score
<
45.0
40.0≤Score<45.0	E	1	Pass
0.0
≤
Score
<
40.0
0.0≤Score<40.0	F	0	Fail
Scores $< 0.0$ or $> 100.0$ must be rejected with an IllegalArgumentException.

Rule 2: Semester GPA Computation
Semester GPA
=
∑
i
=
1
n
(
CreditUnit
i
×
GradePoint
i
)
∑
i
=
1
n
CreditUnit
i
Semester GPA= 
∑ 
i=1
n
​
 CreditUnit 
i
​
 
∑ 
i=1
n
​
 (CreditUnit 
i
​
 ×GradePoint 
i
​
 )
​
 

The computed GPA is rounded to two (2) decimal places.
If a student has no registered courses (
∑
CreditUnit
=
0
∑CreditUnit=0), the GPA defaults to 0.00.
4. Functional Requirements (FR)
Module 1: Student and Course Data
FR-1.1: The system shall retrieve and display all active students (Matriculation Number, Full Name, Department) in the UI selection controls.
FR-1.2: The system shall retrieve and display all approved departmental courses (Course Code, Course Title, Credit Units) in the UI selection controls.
Module 2: Result Entry & Validation
FR-2.1: The system shall allow the user to select a Student, a Course, specify an Academic Session, and input an examination score.
FR-2.2: The system shall validate that the entered score is numeric and falls within $[0, 100]$.
FR-2.3: Upon valid entry, the system shall compute the appropriate letter grade and grade point, saving the result record to the persistent store.
FR-2.4: If a result already exists for the same student, course, and session, the system shall update the existing record without duplicating primary or unique keys.
Module 3: Transactional Batch Operations
FR-3.1: The system shall support recording a batch of course results for a student in a single operation.
FR-3.2: The batch operation must execute under an explicit atomic transaction (connection.setAutoCommit(false)).
FR-3.3: If all records in the batch are valid, the transaction shall commit.
FR-3.4: If any record in the batch violates database constraints (e.g., non-existent course ID or invalid foreign key), the system shall roll back the entire transaction, ensuring zero partial updates are committed.
Module 4: Results Display and GPA Reporting
FR-4.1: Selecting a student and session shall automatically load their complete result ledger into the table view.
FR-4.2: The table view shall present: Course Code, Course Title, Credit Units, Score, Grade, Grade Point, and Weighted Points.
FR-4.3: The system shall calculate and display the updated semester GPA in real-time.
FR-4.4: If no results exist for the selected student and session, the system shall display an informative placeholder and reset GPA to 0.00.
5. Non-Functional Requirements (NFR)
5.1 Data Integrity & Safety
NFR-1 (SQL Injection Immunity): All database interactions must use parameterized SQL statements (java.sql.PreparedStatement). String concatenation in SQL clauses is strictly prohibited.
NFR-2 (Relational Integrity): The schema must enforce foreign key integrity (FOREIGN KEY (student_id) REFERENCES students(id) and FOREIGN KEY (course_id) REFERENCES courses(id)).
NFR-3 (Atomicity): Multi-record database modifications must be protected by explicit transactions with commit and rollback handling.
5.2 Usability and Aesthetics
NFR-4 (UI Separation): The user interface must be defined in declarative FXML, styled using external CSS, and controlled by a dedicated controller class.
NFR-5 (User Feedback): The application must provide visual validation alerts (Alert.AlertType.WARNING and Alert.AlertType.ERROR) and status bar updates upon successful operations.
5.3 Performance & Reliability
NFR-6 (Response Latency): Query execution and GPA recalculation must complete in 
<
100
 ms
<100 ms for standard class sizes (
≤
500
≤500 students).
NFR-7 (Zero Installation Footprint): The database is embedded (SQLite) and requires no external database daemon or network connectivity to run.
6. Use Case Descriptions
Use Case 1: Record Single Course Score
Actor: Examination Officer
Preconditions: Student and Course exist in the database.
Main Flow:
Officer selects Student from dropdown.
Officer selects Course from dropdown.
Officer enters academic session (e.g., 2025/2026) and score (e.g., 78.5).
Officer clicks "Record Score".
System validates score 
∈
[
0
,
100
]
∈[0,100].
System computes Grade (A) and Grade Point (5).
System persists the record via ResultDao.
Table refreshes with new record and recalculates cumulative semester GPA.
Exception Flow:
Score is non-numeric or out of range: System displays an alert warning dialog and aborts the save operation.
Use Case 2: Bulk Semester Result Processing (Transactional)
Actor: Automated Batch Service / Examination Officer
Preconditions: Valid student record exists.
Main Flow:
System receives a list of course IDs and scores for a student.
Service opens a database connection and sets autoCommit = false.
System iterates over courses, computes grades, and executes batch inserts.
Database executes connection.commit().
All records become persistently visible.
Exception Flow:
A course ID does not exist or database constraint fails:
An SQLException is caught.
System invokes connection.rollback().
The database remains in its pre-transaction state with zero partial records inserted.