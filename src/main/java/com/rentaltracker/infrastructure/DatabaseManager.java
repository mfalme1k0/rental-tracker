package com.rentaltracker.infrastructure;

import com.rentaltracker.exception.DatabaseConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private final Path databasePath;


    private final ThreadLocal<Connection> transactionConnection =
            new ThreadLocal<>();

    public DatabaseManager(Path databasePath) {
        this.databasePath = databasePath;
    }

    public Connection getConnection() {
        Connection activeConnection = transactionConnection.get();

        if (activeConnection != null) {
            return activeConnection;
        }

        return createConnection();
    }


    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        if (connection == transactionConnection.get()) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                    "Could not close database connection",
                    e
            );
        }
    }


    public void beginTransaction() {
        if (transactionConnection.get() != null) {
            throw new IllegalStateException(
                    "A transaction is already active"
            );
        }

        Connection connection = createConnection();

        try {
            connection.setAutoCommit(false);
            transactionConnection.set(connection);
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeException) {
                e.addSuppressed(closeException);
            }

            throw new DatabaseConnectionException(
                    "Could not begin database transaction",
                    e
            );
        }
    }


    public void commitTransaction() {
        Connection connection = requireTransactionConnection();

        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                    "Could not commit database transaction",
                    e
            );
        } finally {
            closeTransactionConnection();
        }
    }


    public void rollbackTransaction() {
        Connection connection = requireTransactionConnection();

        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                    "Could not roll back database transaction",
                    e
            );
        } finally {
            closeTransactionConnection();
        }
    }

    public boolean isTransactionActive() {
        return transactionConnection.get() != null;
    }

    public void initialize() {
        try {
            Files.createDirectories(databasePath.toAbsolutePath().getParent());
        } catch (IOException e) {
            throw new DatabaseConnectionException(
                    "Could not create database directory",
                    e
            );
        }

        try (Connection connection = createConnection();
             InputStream input =
                     DatabaseManager.class.getResourceAsStream("/schema.sql")) {

            if (input == null) {
                throw new DatabaseConnectionException(
                        "schema.sql not found"
                );
            }

            String schema =
                    new String(
                            input.readAllBytes(),
                            StandardCharsets.UTF_8
                    );

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

    private Connection createConnection() {
        try {
            String databaseUrl =
                    "jdbc:sqlite:" + databasePath;

            Connection connection =
                    DriverManager.getConnection(databaseUrl);

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

    private Connection requireTransactionConnection() {
        Connection connection = transactionConnection.get();

        if (connection == null) {
            throw new IllegalStateException(
                    "No active database transaction"
            );
        }

        return connection;
    }

    private void closeTransactionConnection() {
        Connection connection = transactionConnection.get();

        transactionConnection.remove();

        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                    "Could not close transaction connection",
                    e
            );
        }
    }
}