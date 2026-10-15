package app.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Execute schema
            Path schemaPath = Paths.get("db/schema.sql");
            if (Files.exists(schemaPath)) {
                String schemaSql = Files.readString(schemaPath);
                for (String sql : schemaSql.split(";")) {
                    if (!sql.trim().isEmpty()) {
                        stmt.execute(sql.trim());
                    }
                }
            }

            // Execute seed data
            Path seedPath = Paths.get("db/seed.sql");
            if (Files.exists(seedPath)) {
                String seedSql = Files.readString(seedPath);
                for (String sql : seedSql.split(";")) {
                    if (!sql.trim().isEmpty()) {
                        stmt.execute(sql.trim());
                    }
                }
            }
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
}
