package com.rentaltracker.infrastructure;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    @Test
    void initializeCreatesAllTables() throws Exception {
        Path database = Files.createTempFile("rental-tracker-", ".db");

        try {
            DatabaseManager databaseManager =
                    new DatabaseManager("jdbc:sqlite:" + database);

            databaseManager.initialize();

            try (Connection connection = databaseManager.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT name FROM sqlite_master " +
                                 "WHERE type = 'table' " +
                                 "AND name IN ('users', 'listed_items', 'rentals') " +
                                 "ORDER BY name")) {

                int tableCount = 0;

                while (resultSet.next()) {
                    tableCount++;
                }

                assertEquals(3, tableCount);
            }
        } finally {
            Files.deleteIfExists(database);
        }
    }
    @Test
    void rejectsItemWithUnknownOwner() throws Exception {
        Path database = Files.createTempFile("rental-tracker-", ".db");

        try {
            DatabaseManager databaseManager =
                    new DatabaseManager("jdbc:sqlite:" + database);

            databaseManager.initialize();

            try (Connection connection = databaseManager.getConnection();
                 Statement statement = connection.createStatement()) {

                SQLException exception = assertThrows(
                        SQLException.class,
                        () -> statement.executeUpdate("""
                            INSERT INTO listed_items
                                (owner_id, name, cost_per_day, status)
                            VALUES
                                (999, 'Bike', 100, 'available')
                            """)
                );

                assertTrue(exception.getMessage().contains("FOREIGN KEY constraint failed"));
            }
        } finally {
            Files.deleteIfExists(database);
        }
    }

    @Test
    void rejectsUserWithNullUsername() throws Exception {
        Path database = Files.createTempFile("rental-tracker-", ".db");

        try {
            DatabaseManager databaseManager =
                    new DatabaseManager("jdbc:sqlite:" + database);

            databaseManager.initialize();

            try (Connection connection = databaseManager.getConnection();
                 Statement statement = connection.createStatement()) {

                SQLException exception = assertThrows(
                        SQLException.class,
                        () -> statement.executeUpdate("""
                            INSERT INTO users (username)
                            VALUES (NULL)
                            """)
                );

                assertTrue(
                        exception.getMessage().contains("NOT NULL constraint failed")
                );
            }
        } finally {
            Files.deleteIfExists(database);
        }
    }
}