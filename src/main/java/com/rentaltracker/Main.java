package com.rentaltracker;

import com.rentaltracker.infrastructure.DatabaseManager;
import com.rentaltracker.repository.UserRepository;
import com.rentaltracker.repository.sqlite.SQLiteUserRepository;
import com.rentaltracker.service.UserService;
import com.rentaltracker.transport.Transport;

/**
 * Composition root: the ONE place that builds the object graph (DatabaseManager -> repositories -> services ->
 * menus) with plain constructor injection.
 *
 * <p>Status: the service layer (domain, exceptions, repository interfaces, UserService, ItemService,
 * RentalService) is implemented and unit-tested against in-memory fakes
 * (see src/test/java/com/rentaltracker/service/fake/). Wiring the real graph here is blocked on two things
 * merging from the other branches:
 */
public final class Main {

    private static final String DATABASE_URL = "jdbc:sqlite:rental-tracker.db";

    private Main() {}

    public static void main(String[] args) {
        System.out.println("Rental tracker: service layer ready, waiting on database and CLI branches to wire up.");

        DatabaseManager databaseManager = new DatabaseManager(DATABASE_URL);
        databaseManager.initialize();

        UserRepository userRepository = new SQLiteUserRepository(databaseManager);
        UserService userService = new UserService(userRepository);

        Transport transport = new Transport(userService);
        transport.start();


    }
}
