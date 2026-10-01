package com.rentaltracker.service;

import com.rentaltracker.domain.Item;
import com.rentaltracker.domain.ItemDetails;
import com.rentaltracker.domain.ItemStatus;
import com.rentaltracker.domain.Rental;
import com.rentaltracker.exception.BusinessRuleException;
import com.rentaltracker.exception.InvalidStateTransitionException;
import com.rentaltracker.exception.ValidationException;
import com.rentaltracker.service.fake.FakeItemRepository;
import com.rentaltracker.service.fake.FakeRentalRepository;
import com.rentaltracker.service.fake.FakeTransactor;
import com.rentaltracker.service.fake.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemServiceTest {

    private FakeItemRepository items;
    private FakeRentalRepository rentals;
    private FakeUserRepository users;
    private ItemService service;
    private long ownerId;

    @BeforeEach
    void setUp() {
        items = new FakeItemRepository();
        rentals = new FakeRentalRepository();
        users = new FakeUserRepository();
        service = new ItemService(items, rentals, users, new FakeTransactor());
        ownerId = users.insert(com.rentaltracker.domain.User.newUser("owner")).id();
    }

    @Test
    void listItemStartsAvailableWithDecimalCost() {
        Item item = service.listItem(ownerId, "Ladder", "d", new BigDecimal("2.50"));
        assertEquals(ItemStatus.AVAILABLE, item.status());
        assertEquals(new BigDecimal("2.50"), item.costPerDay());
    }

    @Test
    void listItemRejectsBlankName() {
        assertThrows(ValidationException.class,
                () -> service.listItem(ownerId, " ", "d", BigDecimal.ONE));
    }

    @Test
    void listItemRejectsZeroOrNegativeCost() {
        assertThrows(ValidationException.class,
                () -> service.listItem(ownerId, "Ladder", "d", BigDecimal.ZERO));
        assertThrows(ValidationException.class,
                () -> service.listItem(ownerId, "Ladder", "d", new BigDecimal("-1")));
    }

    @Test
    void getAvailableItemsExcludesRentedAndUnlisted() {
        service.listItem(ownerId, "Ladder", "d", BigDecimal.ONE);
        Item drill = service.listItem(ownerId, "Drill", "d", BigDecimal.ONE);
        items.updateStatus(drill.id(), ItemStatus.RENTED);

        List<Item> available = service.getAvailableItems(ownerId);
        assertEquals(1, available.size());
        assertEquals("Ladder", available.get(0).name());
    }

    @Test
    void getDetailsIncludesOwnerUsername() {
        Item item = service.listItem(ownerId, "Ladder", "d", BigDecimal.ONE);
        ItemDetails details = service.getDetails(item.id());
        assertEquals("owner", details.ownerUsername());
    }

    @Test
    void delistMovesAvailableToUnlisted() {
        Item item = service.listItem(ownerId, "Ladder", "d", BigDecimal.ONE);
        Item unlisted = service.delist(item.id());
        assertEquals(ItemStatus.UNLISTED, unlisted.status());
    }

    @Test
    void delistTwiceIsRejected() {
        Item item = service.listItem(ownerId, "Ladder", "d", BigDecimal.ONE);
        service.delist(item.id());
        assertThrows(InvalidStateTransitionException.class, () -> service.delist(item.id()));
    }

    @Test
    void relistSucceedsWhenNoActiveRental() {
        Item item = service.listItem(ownerId, "Ladder", "d", BigDecimal.ONE);
        service.delist(item.id());
        Item relisted = service.relist(item.id());
        assertEquals(ItemStatus.AVAILABLE, relisted.status());
    }

    @Test
    void relistIsBlockedWhileAnActiveRentalExists() {
        Item item = service.listItem(ownerId, "Ladder", "d", BigDecimal.ONE);
        items.updateStatus(item.id(), ItemStatus.RENTED);
        service.delist(item.id()); // delisted while out -> UNLISTED, rental still active
        rentals.insert(Rental.newActive(item.id(), 999L, LocalDateTime.now(), LocalDateTime.now().plusDays(1)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.relist(item.id()));
        assertTrue(ex.getMessage().contains("active rental"));
    }
}
