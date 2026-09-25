package com.kood.rentaltracker.service;

import com.kood.rentaltracker.domain.User;
import com.kood.rentaltracker.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Account rules. SKELETON: signatures are the contract the transport layer builds against; bodies are filled in
 * by the service-layer PRs.
 */
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = Objects.requireNonNull(users);
    }

    /** The app owner if an account already exists (the lowest-id user); empty on first launch. */
    public Optional<User> findOwner() {
        throw new UnsupportedOperationException("TODO service PR: users.findFirst()");
    }

    /**
     * First launch: validates and stores the owner account.
     *
     * @throws com.kood.rentaltracker.exception.ValidationException blank username
     */
    public User registerOwner(String username) {
        throw new UnsupportedOperationException("TODO service PR: validate, users.insert(User.newUser(..))");
    }

    /** Reuses the user with this username, or creates it; never duplicates. */
    public User findOrCreateRenter(String username) {
        throw new UnsupportedOperationException("TODO service PR: findByUsername or insert");
    }
}
