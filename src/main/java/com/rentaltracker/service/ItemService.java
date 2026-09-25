package com.rentaltracker.service;

import com.rentaltracker.domain.Item;
import com.rentaltracker.domain.ItemDetails;
import com.rentaltracker.repository.ItemRepository;
import com.rentaltracker.repository.RentalRepository;
import com.rentaltracker.repository.Transactor;
import com.rentaltracker.repository.UserRepository;

import java.util.List;
import java.util.Objects;

/** Listing, inventory, delist/relist. SKELETON: see UserService. */
public class ItemService {

    private final ItemRepository items;
    private final RentalRepository rentals;
    private final UserRepository users;
    private final Transactor transactor;

    public ItemService(ItemRepository items, RentalRepository rentals, UserRepository users, Transactor transactor) {
        this.items = Objects.requireNonNull(items);
        this.rentals = Objects.requireNonNull(rentals);
        this.users = Objects.requireNonNull(users);
        this.transactor = Objects.requireNonNull(transactor);
    }

    /**
     * Creates a listing with status AVAILABLE.
     *
     * @throws com.rentaltracker.exception.ValidationException blank name, or cost per day below 1
     */
    public Item listItem(long ownerId, String name, String description, int costPerDay) {
        throw new UnsupportedOperationException("TODO service PR");
    }

    /** Every item the owner has, whatever its status. */
    public List<Item> getInventory(long ownerId) {
        throw new UnsupportedOperationException("TODO service PR");
    }

    /** Only AVAILABLE items: the "record a rental" list. */
    public List<Item> getAvailableItems(long ownerId) {
        throw new UnsupportedOperationException("TODO service PR");
    }

    /** Item plus owner username for the detail screen. @throws com.rentaltracker.exception.NotFoundException */
    public ItemDetails getDetails(long itemId) {
        throw new UnsupportedOperationException("TODO service PR");
    }

    /**
     * AVAILABLE or RENTED -> UNLISTED.
     *
     * @throws com.rentaltracker.exception.InvalidStateTransitionException already unlisted
     */
    public Item delist(long itemId) {
        throw new UnsupportedOperationException("TODO service PR");
    }

    /**
     * UNLISTED -> AVAILABLE, only when the item has no active rental.
     *
     * @throws com.rentaltracker.exception.BusinessRuleException the item is still out on rental
     * @throws com.rentaltracker.exception.InvalidStateTransitionException the item is not unlisted
     */
    public Item relist(long itemId) {
        throw new UnsupportedOperationException("TODO service PR");
    }
}
