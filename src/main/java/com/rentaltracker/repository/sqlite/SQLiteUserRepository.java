package com.rentaltracker.repository.sqlite;

import com.rentaltracker.domain.User;
import com.rentaltracker.exception.MappingException;
import com.rentaltracker.exception.NotFoundException;
import com.rentaltracker.infrastructure.DatabaseManager;
import com.rentaltracker.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

public class SQLiteUserRepository implements UserRepository {

    private final DatabaseManager databaseManager;

    public SQLiteUserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public User insert(User user) {
        String insertSql = """
                INSERT INTO users (username)
                VALUES (?)
                """;

        String selectSql = """
                SELECT id, username, created_at
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(
                     insertSql,
                     Statement.RETURN_GENERATED_KEYS)) {

            insertStatement.setString(1, user.username());
            insertStatement.executeUpdate();

            long id;

            try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("No generated key returned for inserted user");
                }

                id = generatedKeys.getLong(1);
            }

            try (PreparedStatement selectStatement =
                         connection.prepareStatement(selectSql)) {

                selectStatement.setLong(1, id);

                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException(
                                "Inserted user could not be found: " + id
                        );
                    }

                    return mapUser(resultSet);
                }
            }

        } catch (SQLException e) {
            throw SQLiteExceptionTranslator.translate(e);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<User> findByUsername(String username) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<User> findFirst() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public User getById(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private User mapUser(ResultSet resultSet) {
        try {
            long id = resultSet.getLong("id");
            String username = resultSet.getString("username");
            String createdAtValue = resultSet.getString("created_at");

            LocalDateTime createdAt =
                    OffsetDateTime.parse(createdAtValue).toLocalDateTime();

            return new User(
                    id,
                    username,
                    null,
                    createdAt
            );

        } catch (SQLException | RuntimeException e) {
            throw new MappingException(
                    "Could not map database row to User",
                    e
            );
        }
    }
}

