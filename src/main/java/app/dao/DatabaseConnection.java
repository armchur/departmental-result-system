package app.dao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_DIR = "db";
    private static final String DB_FILE = "db/result_system.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    static {
        try {
            Path dirPath = Paths.get(DB_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (Exception e) {
            System.err.println("Failed to create database directory: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }
}
