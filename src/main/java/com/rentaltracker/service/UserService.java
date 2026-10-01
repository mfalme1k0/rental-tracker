package com.rentaltracker.service;

import com.rentaltracker.domain.User;
import com.rentaltracker.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

// Account rules.
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = Objects.requireNonNull(users);
    }

    /**
     * The app owner, if the account has already been set up. There is only ever one owner account, and it is always the first account created.
     */
    public Optional<User> findOwner() {
        return users.findFirst();
    }

    /**
     * First launch: validates and stores the owner account.
     *
     * @throws com.rentaltracker.exception.ValidationException blank username
     * @throws com.rentaltracker.exception.UniqueConstraintException an account already exists with this username
     *         (should not normally happen on a true first launch, but the database is still the final guard)
     */
    public User registerOwner(String username) {
        String clean = Validation.requireNonBlank(username, "username");
        return users.insert(User.newUser(clean));
    }

    /**
     * Reuses the account with this username if one exists, otherwise creates it. Used both for the owner on
     * later launches (transport looks the owner up by username it already knows) and for renters typed in at
     * the "record a rental" screen.
     *
     * @throws com.rentaltracker.exception.ValidationException blank username
     */
    public User findOrCreateRenter(String username) {
        String clean = Validation.requireNonBlank(username, "username");
        return users.findByUsername(clean).orElseGet(() -> users.insert(User.newUser(clean)));
    }
}
