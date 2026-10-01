package com.rentaltracker.domain;

import java.time.LocalDateTime;

/**
 * An account: the owner of the items, or a renter created from a name. {@code password} is always null in
 * the database.
 */
public record User(Long id, String username, String password, LocalDateTime createdAt) {

    /** A user that has not been saved yet. */
    public static User newUser(String username) {
        return new User(null, username, null, null);
    }
}
