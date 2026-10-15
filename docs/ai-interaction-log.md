# AI Interaction Log - CSC 508 Team Project

## Entry 1
- **Phase:** Setup & Build Configuration (Week 8)
- **Tool:** Lovable / AI Assistant
- **Task:** Configure Maven dependencies for JavaFX 21, SQLite JDBC, and JUnit 5 in `pom.xml`.
- **Prompt:** "The Departmental Result System with SQLite — start with the project skeleton and database schema."
- **Outcome & Judgement:** Accepted with modification. Ensured `<maven.compiler.source>` was set to 17 to match the course requirement, and verified that JUnit Jupiter was scoped strictly to `test`.

## Entry 2
- **Phase:** Phase 3 — Vertical Slice (DAO & Database)
- **Tool:** Lovable / AI Assistant
- **Task:** Implement SQLite DAO layer for student and course result management.
- **Prompt:** Generated `SqliteResultDao.java` matching the `ResultDao` interface.
- **Outcome & Judgement:** Verified. Confirmed that all SQL queries strictly use parameterized `PreparedStatement` to satisfy Section 5 constraint against SQL concatenation. Added `ON CONFLICT` clause to prevent duplicate student-course-session entries.

## Entry 3
- **Phase:** Troubleshooting / Compiler Resolution
- **Tool:** Lovable / AI Assistant
- **Task:** Resolve `cannot find symbol: variable DatabaseConnection` during `mvn clean javafx:run`.
- **Prompt:** Reported compiler error log to AI.
- **Outcome & Judgement:** Identified that `DatabaseConnection.java` had either a naming or path mismatch. Re-saved file with correct package statement `app.dao`. Build succeeded.
