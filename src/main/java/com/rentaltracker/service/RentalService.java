package com.rentaltracker.service;

import com.rentaltracker.domain.Item;
import com.rentaltracker.domain.ItemStatus;
import com.rentaltracker.domain.Rental;
import com.rentaltracker.domain.RentalDetails;
import com.rentaltracker.domain.RentalStatus;
import com.rentaltracker.domain.User;
import com.rentaltracker.exception.BusinessRuleException;
import com.rentaltracker.repository.ItemRepository;
import com.rentaltracker.repository.RentalRepository;
import com.rentaltracker.repository.Transactor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Rent and return.
 *
 * <p>The {@link Clock} is injected rather than calling {@code LocalDateTime.now()} directly, so
 * "end time = start time + days" can be tested against a fixed instant instead of a moving target.
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
     * Records a rental: resolves or creates the renter, checks the renter is not the owner, moves the item
     * AVAILABLE -> RENTED, and inserts the rental with end = start + days.
     * @throws com.rentaltracker.exception.NotFoundException no item with this id
     * @throws com.rentaltracker.exception.ValidationException blank renter name, or days below the team's
     *         minimum of 1
     * @throws BusinessRuleException the renter is the item's own owner (team decision: self-rental is not allowed)
     * @throws com.rentaltracker.exception.InvalidStateTransitionException the item is not AVAILABLE (already
     *         rented, or unlisted)
     */
    public Rental recordRental(long itemId, String renterName, int days) {
        String cleanRenterName = Validation.requireNonBlank(renterName, "renterName");
        int validDays = Validation.requireAtLeastMinimumDays(days);

        return transactor.inTransaction(() -> {
            Item item = items.getById(itemId);
            User renter = userService.findOrCreateRenter(cleanRenterName);

            // The owner may not rent their own item. Checked by identity (user id), not by
            // comparing names, so it still catches the case where the renter types the owner's own username.
            if (Objects.equals(renter.id(), item.ownerId())) {
                throw new BusinessRuleException("The owner of an item cannot rent their own item");
            }

            Item rentedItem = item.transitionTo(ItemStatus.RENTED);
            items.updateStatus(itemId, rentedItem.status());

            LocalDateTime start = LocalDateTime.now(clock);
            LocalDateTime end = start.plusDays(validDays);
            return rentals.insert(Rental.newActive(itemId, renter.id(), start, end));
        });
    }

    /**
     * Active rentals across the owner's items, oldest due date first (the "confirm a return" list).
     *
     * <p>Filtered by RENTAL status, not item status, on purpose: an item delisted while it was out is UNLISTED,
     * but its rental is still active and must remain returnable. Filtering by item status would hide it and
     * leave it un-returnable forever (see docs/architecture.md, section 4).
     */
    public List<RentalDetails> getActiveRentals(long ownerId) {
        return rentals.findActiveDetailsByOwnerId(ownerId);
    }

    /**
     * Confirms a return: closes the rental with a return timestamp, and puts the item back to AVAILABLE
     * <em>only if it is still RENTED</em>. If it was delisted while out (now UNLISTED), it stays UNLISTED — the
     * return does not silently re-list an item the owner deliberately took down.
     * @throws com.rentaltracker.exception.NotFoundException no rental with this id
     * @throws BusinessRuleException the rental is not currently active (already returned)
     */
    public Rental confirmReturn(long rentalId) {
        return transactor.inTransaction(() -> {
            Rental rental = rentals.getById(rentalId);
            if (rental.status() != RentalStatus.ACTIVE) {
                throw new BusinessRuleException("Rental " + rentalId + " is not active, it cannot be returned");
            }

            LocalDateTime returnedAt = LocalDateTime.now(clock);
            Rental closed = rental.closedAt(returnedAt);
            rentals.updateStatus(rentalId, closed.status(), returnedAt);

            Item item = items.getById(rental.itemId());
            if (item.status() == ItemStatus.RENTED) {
                Item available = item.transitionTo(ItemStatus.AVAILABLE);
                items.updateStatus(item.id(), available.status());
            }
            // else: item is UNLISTED (delisted while out) - leave it as the owner set it.

            return closed;
        });
    }
}
