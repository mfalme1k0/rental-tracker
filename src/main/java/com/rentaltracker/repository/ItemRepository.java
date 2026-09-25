package com.rentaltracker.repository;

import com.rentaltracker.domain.Item;
import com.rentaltracker.domain.ItemStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for items. There is no delete: items are soft-removed through the UNLISTED status.
 * List methods return an empty list, never null.
 */
public interface ItemRepository {

    /**
     * @return the saved item with id and createdAt filled in.
     * @throws com.rentaltracker.exception.NotNullConstraintException missing required field
     * @throws com.rentaltracker.exception.ForeignKeyConstraintException unknown owner
     */
    Item insert(Item item);

    Optional<Item> findById(long id);

    /** @throws com.rentaltracker.exception.NotFoundException when no row has this id */
    Item getById(long id);

    /** Every item the owner has, in any status, oldest first (the inventory screen). */
    List<Item> findByOwnerId(long ownerId);

    /** The owner's items in one status, oldest first (e.g. AVAILABLE for the "record a rental" list). */
    List<Item> findByOwnerIdAndStatus(long ownerId, ItemStatus status);

    /**
     * @throws com.rentaltracker.exception.NotFoundException when no row has this id (0 rows updated)
     * @throws com.rentaltracker.exception.CheckConstraintException status rejected by the database
     */
    void updateStatus(long itemId, ItemStatus status);
}
