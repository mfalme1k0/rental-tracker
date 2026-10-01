package com.rentaltracker.service.fake;

import com.rentaltracker.domain.Item;
import com.rentaltracker.domain.ItemStatus;
import com.rentaltracker.exception.NotFoundException;
import com.rentaltracker.repository.ItemRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeItemRepository implements ItemRepository {

    private final Map<Long, Item> byId = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Item insert(Item item) {
        Item saved = new Item(nextId.getAndIncrement(), item.ownerId(), item.name(), item.description(),
                item.costPerDay(), item.status(), LocalDateTime.now());
        byId.put(saved.id(), saved);
        return saved;
    }

    @Override
    public Optional<Item> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Item getById(long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("item " + id + " not found"));
    }

    @Override
    public List<Item> findByOwnerId(long ownerId) {
        return byId.values().stream().filter(i -> i.ownerId() == ownerId).collect(Collectors.toList());
    }

    @Override
    public List<Item> findByOwnerIdAndStatus(long ownerId, ItemStatus status) {
        return byId.values().stream()
                .filter(i -> i.ownerId() == ownerId && i.status() == status)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(long itemId, ItemStatus status) {
        Item current = getById(itemId);
        byId.put(itemId, current.withStatus(status));
    }
}
