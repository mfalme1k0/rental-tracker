package com.rentaltracker.repository.sqlite;

import com.rentaltracker.infrastructure.DatabaseManager;
import com.rentaltracker.repository.Transactor;

import java.util.Objects;
import java.util.function.Supplier;

public final class SQLiteTransactor implements Transactor {

    private final DatabaseManager databaseManager;

    public SQLiteTransactor(DatabaseManager databaseManager) {
        this.databaseManager =
                Objects.requireNonNull(databaseManager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        Objects.requireNonNull(work);


        if (databaseManager.isTransactionActive()) {
            return work.get();
        }

        databaseManager.beginTransaction();

        try {
            T result = work.get();

            databaseManager.commitTransaction();

            return result;

        } catch (RuntimeException e) {
            databaseManager.rollbackTransaction();
            throw e;
        }
    }
}