package com.rentaltracker.domain;

import com.rentaltracker.exception.InvalidStateTransitionException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Item lifecycle as a state machine. The ONLY place that knows which moves are legal:
 *
 * <pre>
 *   (list)      -> AVAILABLE
 *   AVAILABLE   -> RENTED     record rental
 *   AVAILABLE   -> UNLISTED   delist
 *   RENTED      -> AVAILABLE  confirm return
 *   RENTED      -> UNLISTED   delist while out
 *   UNLISTED    -> AVAILABLE  relist (service additionally requires: no active rental)
 * </pre>
 *
 * The "no active rental" guard for relisting is a cross-entity rule (it needs the rentals table), so it lives in
 * {@code ItemService}, not here. This enum answers only "is this pair of statuses a legal move?".
 */
public enum ItemStatus {
    AVAILABLE("available"),
    RENTED("rented"),
    UNLISTED("unlisted");

    private final String dbValue;

    ItemStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    /** The exact text stored in {@code listed_items.status}; must match the CHECK constraint in schema.sql. */
    public String dbValue() {
        return dbValue;
    }

    /**
     * @throws IllegalArgumentException for unknown text. The repository wraps this in a
     *         {@code MappingException}; the domain stays free of persistence exceptions.
     */
    public static ItemStatus fromDbValue(String value) {
        for (ItemStatus status : values()) {
            if (status.dbValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown item status: " + value);
    }

    public Set<ItemStatus> allowedTransitions() {
        return switch (this) {
            case AVAILABLE -> EnumSet.of(RENTED, UNLISTED);
            case RENTED -> EnumSet.of(AVAILABLE, UNLISTED);
            case UNLISTED -> EnumSet.of(AVAILABLE);
        };
    }

    public boolean canTransitionTo(ItemStatus target) {
        return allowedTransitions().contains(target);
    }

    /** @return {@code target} if the move is legal. @throws InvalidStateTransitionException otherwise. */
    public ItemStatus transitionTo(ItemStatus target) {
        if (!canTransitionTo(target)) {
            throw new InvalidStateTransitionException(
                    "Cannot change an item from " + dbValue + " to " + target.dbValue);
        }
        return target;
    }
}
