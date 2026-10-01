package com.rentaltracker.service;

import com.rentaltracker.domain.User;
import com.rentaltracker.exception.ValidationException;
import com.rentaltracker.service.fake.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(new FakeUserRepository());
    }

    @Test
    void findOwnerIsEmptyBeforeRegistration() {
        assertTrue(service.findOwner().isEmpty());
    }

    @Test
    void registerOwnerSavesTheFirstUserAsOwner() {
        User owner = service.registerOwner("liisa");
        assertEquals(Optional.of(owner), service.findOwner());
    }

    @Test
    void registerOwnerRejectsBlankUsername() {
        assertThrows(ValidationException.class, () -> service.registerOwner("   "));
        assertThrows(ValidationException.class, () -> service.registerOwner(null));
    }

    @Test
    void findOrCreateRenterReusesAnExistingAccount() {
        User first = service.findOrCreateRenter("meelis");
        User second = service.findOrCreateRenter("meelis");
        assertEquals(first.id(), second.id());
    }

    @Test
    void findOrCreateRenterCreatesWhenAbsent() {
        User renter = service.findOrCreateRenter("uku");
        assertEquals("uku", renter.username());
    }

    @Test
    void findOrCreateRenterRejectsBlankName() {
        assertThrows(ValidationException.class, () -> service.findOrCreateRenter(" "));
    }
}
