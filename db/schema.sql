PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    matric_no TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    department TEXT NOT NULL,
    level INTEGER NOT NULL CHECK (level IN (100, 200, 300, 400, 500))
);

CREATE TABLE IF NOT EXISTS courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    credit_unit INTEGER NOT NULL CHECK (credit_unit > 0 AND credit_unit <= 6),
    semester INTEGER NOT NULL CHECK (semester IN (1, 2))
);

CREATE TABLE IF NOT EXISTS results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    session TEXT NOT NULL,
    score REAL NOT NULL CHECK (score >= 0.0 AND score <= 100.0),
    grade TEXT NOT NULL,
    grade_point INTEGER NOT NULL CHECK (grade_point BETWEEN 0 AND 5),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE(student_id, course_id, session)
);
