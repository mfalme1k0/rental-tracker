package com.rentaltracker;

import com.rentaltracker.infrastructure.DatabaseManager;
import com.rentaltracker.repository.ItemRepository;
import com.rentaltracker.repository.RentalRepository;
import com.rentaltracker.repository.Transactor;
import com.rentaltracker.repository.UserRepository;
import com.rentaltracker.repository.sqlite.SQLiteItemRepository;
import com.rentaltracker.repository.sqlite.SQLiteRentalRepository;
import com.rentaltracker.repository.sqlite.SQLiteTransactor;
import com.rentaltracker.repository.sqlite.SQLiteUserRepository;
import com.rentaltracker.service.ItemService;
import com.rentaltracker.service.RentalService;
import com.rentaltracker.service.UserService;
import com.rentaltracker.transport.Transport;

import java.nio.file.Path;
import java.time.Clock;

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

    private static final Path DATABASE_PATH = Path.of("rental-tracker.db");

    private Main() {}

    public static void main(String[] args) {
        System.out.println("Rental Tracker");

        DatabaseManager databaseManager = new DatabaseManager(DATABASE_PATH);
        databaseManager.initialize();

        UserRepository userRepository = new SQLiteUserRepository(databaseManager);
        UserService userService = new UserService(userRepository);

        ItemRepository itemRepository = new SQLiteItemRepository(databaseManager);
        RentalRepository rentalRepository = new SQLiteRentalRepository(databaseManager);
        Transactor transactor = new SQLiteTransactor(databaseManager);

        ItemService itemService = new ItemService(
                itemRepository,
                rentalRepository,
                userRepository,
                transactor);

        Clock clock = Clock.systemDefaultZone();

        RentalService rentalService = new RentalService(
                itemRepository,
                rentalRepository,
                userService,
                transactor,
                clock
        );

        Transport transport = new Transport(
                userService,
                itemService,
                rentalService
        );

        transport.start();
    }
}
