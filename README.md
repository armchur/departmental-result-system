# Departmental Result Management System (CSC 508 Coursework)

A desktop departmental result processing application built using Java 17, JavaFX 21, SQLite 3, and Apache Maven. The system adheres strictly to a 3-tier layered architecture (Presentation, Business/Service, Data Access Layer) and enforces automated business rules, transactional data integrity, and parameterized database operations.

---

## 1. System Architecture & Design Principles

The application is structured according to strict separation of concerns:

- **Presentation Layer (`app.ui`)**:
  - Built with **JavaFX 21** using declarative **FXML** (`views/ResultView.fxml`) and an external stylesheet (`css/style.css`).
  - Controlled by `ResultViewController.java`. The presentation layer contains **zero SQL** and delegates all calculations, validation, and persistence to the service layer.
- **Service Layer (`app.service`)**:
  - Implemented in `ResultService.java`.
  - Encapsulates domain logic: input validation, 5-point university grading scale (`A=5`, `B=4`, `C=3`, `D=2`, `E=1`, `F=0`), weighted grade point and GPA calculation, and transactional bulk-record orchestration.
- **Persistence / Data Access Layer (`app.dao`)**:
  - Defined by the interface `ResultDao.java` and implemented by `SqliteResultDao.java`.
  - Uses standard JDBC with SQLite (`sqlite-jdbc`).
  - **100% parameterized queries** via `PreparedStatement` to eliminate SQL injection risks.
  - Multi-write operations (bulk semester grade recording) are executed within explicit ACID transactions (`connection.setAutoCommit(false)`, `commit()`, and `rollback()` on failure).
- **Data Models (`app.model`)**:
  - Strongly typed domain entities: `Student`, `Course`, and `ResultEntry`.

---

## 2. Technology Stack & Prerequisites

- **Language**: Java 17 (or newer LTS, e.g. Java 21)
- **GUI Framework**: JavaFX 21 (`javafx-controls`, `javafx-fxml`)
- **Build Tool**: Apache Maven 3.8+
- **Database**: SQLite 3 (embedded via `org.xerial:sqlite-jdbc`)
- **Testing**: JUnit 5 Jupiter (`5.10.2`)
- **IDE**: VS Code (with Extension Pack for Java)

---

## 3. Project Directory Structure

```text
DepartmentalResultSystem/
├── .gitignore
├── pom.xml
├── README.md
├── db/
│   ├── schema.sql                 # DDL definitions with FKs and constraints
│   ├── seed.sql                   # Sample students, courses, and historical results
│   └── departmental_results.db   # Auto-created SQLite database file
├── docs/
│   ├── SRS.md                     # Software Requirements Specification
│   ├── Design.md                  # Architectural diagrams, ERD, and design patterns
│   └── AI_Log.md                  # AI Interaction & Academic Integrity Log
├── src/
│   ├── main/
│   │   ├── java/app/
│   │   │   ├── dao/               # ResultDao, SqliteResultDao, DatabaseConnection, DatabaseInitializer
│   │   │   ├── model/             # Student, Course, ResultEntry
│   │   │   ├── service/           # ResultService (Grading, GPA, Transactions)
│   │   │   └── ui/                # MainApp, ResultViewController
│   │   └── resources/
│   │       ├── css/
│   │       │   └── style.css      # Custom UI stylesheet
│   │       └── views/
│   │           └── ResultView.fxml# Presentation view definition
│   └── test/
│       └── java/app/service/
│           └── ResultServiceTest.java # 17 automated JUnit 5 tests


4. Setup, Build, and Execution Instructions
A. Clone and Open Project
Open the root directory DepartmentalResultSystem in VS Code or navigate to it in Command Prompt:
cd C:\DepartmentalResultSystem


B. Run Automated Unit Tests



Execute the full JUnit 5 test suite via Maven:



mvn clean test




Expected output:



[INFO] Running app.service.ResultServiceTest
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS




Test coverage includes:





Exact grade boundary tests (A down to F).


Boundary exception tests (negative scores, scores > 100).


GPA calculations with multiple course credit weights and empty result sets.


Successful transactional commit of multiple course scores.


Transaction rollback verification (ensuring zero partial writes when an error occurs mid-batch).




C. Run the Application



Launch the JavaFX desktop interface:



mvn javafx:run




The database (db/departmental_results.db) will automatically initialize from db/schema.sql and db/seed.sql on first launch.





5. Key Features




Student & Course Selection: Dynamic dropdown population queried from the database through the service layer.


Real-time GPA Computation: Automatically computes total credit units, weighted points, and semester GPA rounded to two decimal places upon result entry or student selection.


Robust Input Validation: Validates user inputs on the frontend and asserts business boundaries in the service layer before persistence.


Data Integrity & Safety: Foreign key constraints enforced by SQLite; all database queries use parameter placeholders (?).


Modern Desktop UI: Styled responsive layout with clean contrast, distinct status feedback, and a dedicated table view.