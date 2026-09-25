package com.rentaltracker.domain;

import java.time.LocalDateTime;

/**
 * An account: the owner of the items, or a renter created from a name. {@code password} is always null in
 * project 1 (kept so project 2 needs no schema change).
 *
 * <p>Records perform NO validation on purpose. Tests must be able to build an invalid object (e.g. an Item with a
 * null name) and push it at the repository to prove the DATABASE rejects it. Business validation lives in the
 * service layer.
 *
 * @param id        null until the row has been inserted
 * @param createdAt null until inserted; set by the database, stored/read as UTC
 */
public record User(Long id, String username, String password, LocalDateTime createdAt) {

    /** A user that has not been saved yet. */
    public static User newUser(String username) {
        return new User(null, username, null, null);
    }
}
