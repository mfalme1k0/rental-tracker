package com.rentaltracker.service.fake;

import com.rentaltracker.repository.Transactor;

import java.util.function.Supplier;

/**
 * Runs work inline with no real rollback. Good enough for service-layer unit tests, which check business
 * logic (what gets called, in what order, with what result) rather than SQLite's transaction guarantees —
 * that belongs to repository-layer integration tests against a real database.
 */
public class FakeTransactor implements Transactor {

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return work.get();
    }
}
