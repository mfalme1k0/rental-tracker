package com.rentaltracker.infrastructure;

import com.rentaltracker.exception.DatabaseConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private final String databaseUrl;

    public DatabaseManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl);

            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }

            return connection;
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                    "Could not connect to database",
                    e
            );
        }
    }

    public void initialize() {
        try (Connection connection = getConnection();
             InputStream input = DatabaseManager.class.getResourceAsStream("/schema.sql")) {

            if (input == null) {
                throw new DatabaseConnectionException("schema.sql not found");
            }

            String schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(schema);
            }

        } catch (IOException | SQLException e) {
            throw new DatabaseConnectionException(
                    "Could not initialize database",
                    e
            );
        }
    }
}