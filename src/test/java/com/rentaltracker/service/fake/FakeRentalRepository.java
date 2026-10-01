package com.rentaltracker.service.fake;

import com.rentaltracker.domain.Rental;
import com.rentaltracker.domain.RentalDetails;
import com.rentaltracker.domain.RentalStatus;
import com.rentaltracker.exception.NotFoundException;
import com.rentaltracker.repository.RentalRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeRentalRepository implements RentalRepository {

    private final Map<Long, Rental> byId = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Rental insert(Rental rental) {
        Rental saved = new Rental(nextId.getAndIncrement(), rental.itemId(), rental.renterId(),
                rental.startTime(), rental.endTime(), rental.returnedAt(), rental.status());
        byId.put(saved.id(), saved);
        return saved;
    }

    @Override
    public Optional<Rental> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Rental getById(long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("rental " + id + " not found"));
    }

    @Override
    public List<Rental> findByItemId(long itemId) {
        return byId.values().stream().filter(r -> r.itemId() == itemId).collect(Collectors.toList());
    }

    @Override
    public Optional<Rental> findActiveByItemId(long itemId) {
        return byId.values().stream()
                .filter(r -> r.itemId() == itemId && r.status() == RentalStatus.ACTIVE)
                .findFirst();
    }

    @Override
    public List<RentalDetails> findActiveDetailsByOwnerId(long ownerId) {
        // Fake has no join; tests that need this populate byId directly and don't rely on owner filtering here.
        return byId.values().stream()
                .filter(r -> r.status() == RentalStatus.ACTIVE)
                .map(r -> new RentalDetails(r, "item-" + r.itemId(), "renter-" + r.renterId()))
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(long rentalId, RentalStatus status, LocalDateTime returnedAt) {
        Rental current = getById(rentalId);
        byId.put(rentalId, new Rental(current.id(), current.itemId(), current.renterId(),
                current.startTime(), current.endTime(), returnedAt, status));
    }
}
