package com.rentaltracker.service.fake;

import com.rentaltracker.domain.User;
import com.rentaltracker.exception.UniqueConstraintException;
import com.rentaltracker.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory stand-in for the real SQLite repository, preserving insertion order like the real "lowest id" rule. */
public class FakeUserRepository implements UserRepository {

    private final Map<Long, User> byId = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public User insert(User user) {
        boolean duplicate = byId.values().stream().anyMatch(u -> u.username().equals(user.username()));
        if (duplicate) {
            throw new UniqueConstraintException("username already exists: " + user.username());
        }
        User saved = new User(nextId.getAndIncrement(), user.username(), user.password(), LocalDateTime.now());
        byId.put(saved.id(), saved);
        return saved;
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return byId.values().stream().filter(u -> u.username().equals(username)).findFirst();
    }

    @Override
    public Optional<User> findFirst() {
        return byId.values().stream().findFirst();
    }

    @Override
    public User getById(long id) {
        return findById(id).orElseThrow();
    }
}
