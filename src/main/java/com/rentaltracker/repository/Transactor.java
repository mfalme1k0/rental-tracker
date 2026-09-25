package com.rentaltracker.repository;

import java.util.function.Supplier;

/**
 * Runs work atomically: everything inside commits together or rolls back together.
 *
 * <p>Why an interface here: transactions are a BUSINESS decision ("recording a rental = insert rental + flip
 * item status, all or nothing"), so the service layer decides where they start and end. But the service must
 * not touch {@code java.sql} or the infrastructure package (see docs/architecture.md), so it depends on this
 * abstraction and {@code repository.sqlite.SqliteTransactor} implements it on top of the DatabaseManager.
 *
 * <p>Contract: a RuntimeException thrown by {@code work} rolls the transaction back and is rethrown unchanged;
 * a nested call joins the outer transaction.
 */
public interface Transactor {

    <T> T inTransaction(Supplier<T> work);

    default void inTransaction(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }
}
