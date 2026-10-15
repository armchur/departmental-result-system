package app.dao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        Path schemaPath = Paths.get("db/schema.sql");
        if (!Files.exists(schemaPath)) {
            throw new IllegalStateException("CRITICAL: Schema file not found at " + schemaPath.toAbsolutePath());
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Execute schema
            String schemaSql = Files.readString(schemaPath);
            for (String sql : schemaSql.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql.trim());
                }
            }

            // Execute seed data if present
            Path seedPath = Paths.get("db/seed.sql");
            if (Files.exists(seedPath)) {
                String seedSql = Files.readString(seedPath);
                for (String sql : seedSql.split(";")) {
                    if (!sql.trim().isEmpty()) {
                        stmt.execute(sql.trim());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }
}
