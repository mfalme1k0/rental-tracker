package com.rentaltracker.repository;

import java.util.function.Supplier;

/**
 * Runs work atomically: everything inside commits together or rolls back together.
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
