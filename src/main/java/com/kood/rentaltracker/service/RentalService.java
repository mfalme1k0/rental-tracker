package com.kood.rentaltracker.service;

import com.kood.rentaltracker.domain.Rental;
import com.kood.rentaltracker.domain.RentalDetails;
import com.kood.rentaltracker.repository.ItemRepository;
import com.kood.rentaltracker.repository.RentalRepository;
import com.kood.rentaltracker.repository.Transactor;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

/**
 * Rent and return. SKELETON: see UserService.
 *
 * <p>The Clock is injected so "end time = start time + days" is testable with a fixed instant.
 */
public class RentalService {

    private final ItemRepository items;
    private final RentalRepository rentals;
    private final UserService userService;
    private final Transactor transactor;
    private final Clock clock;

    public RentalService(ItemRepository items, RentalRepository rentals, UserService userService,
                         Transactor transactor, Clock clock) {
        this.items = Objects.requireNonNull(items);
        this.rentals = Objects.requireNonNull(rentals);
        this.userService = Objects.requireNonNull(userService);
        this.transactor = Objects.requireNonNull(transactor);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * One transaction: item AVAILABLE -> RENTED, renter found-or-created, rental inserted with
     * end = start + days.
     *
     * @throws com.kood.rentaltracker.exception.InvalidStateTransitionException item rented or unlisted
     * @throws com.kood.rentaltracker.exception.ValidationException blank renter name or days below 1
     */
    public Rental recordRental(long itemId, String renterName, int days) {
        throw new UnsupportedOperationException("TODO service PR");
    }

    /** Active rentals of the owner's items, sorted by due date (includes items delisted while out). */
    public List<RentalDetails> getActiveRentals(long ownerId) {
        throw new UnsupportedOperationException("TODO service PR");
    }

    /**
     * One transaction: rental -> CLOSED with returned_at; item RENTED -> AVAILABLE, but an item delisted while
     * out stays UNLISTED. The rental row is never deleted.
     *
     * @throws com.kood.rentaltracker.exception.BusinessRuleException rental is not active
     */
    public Rental confirmReturn(long rentalId) {
        throw new UnsupportedOperationException("TODO service PR");
    }
}
