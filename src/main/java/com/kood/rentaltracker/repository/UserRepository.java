package com.kood.rentaltracker.repository;

import com.kood.rentaltracker.domain.User;

import java.util.Optional;

/**
 * Persistence contract for users. Every method may throw a DatabaseException subtype; implementations translate
 * SQLException, callers never see it.
 */
public interface UserRepository {

    /**
     * @return the saved user with id and createdAt filled in.
     * @throws com.kood.rentaltracker.exception.UniqueConstraintException duplicate username
     * @throws com.kood.rentaltracker.exception.NotNullConstraintException missing username
     */
    User insert(User user);

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    /**
     * The lowest-id user: by rule this is the app owner (created on first launch, before any renter exists).
     * Empty means "first launch".
     */
    Optional<User> findFirst();

    /** @throws com.kood.rentaltracker.exception.NotFoundException when no row has this id */
    User getById(long id);
}
