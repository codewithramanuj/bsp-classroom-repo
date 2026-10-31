package com.bsp.supportassistant.repository;

import com.bsp.supportassistant.model.Ticket;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Keeps the tickets in memory, so the example needs no database. They are lost when the application stops.
@Repository
public class InMemoryTicketRepository implements TicketRepository {

    private final Map<Long, Ticket> tickets = new ConcurrentHashMap<>();
    private final AtomicLong lastId = new AtomicLong();

    @Override
    public Ticket save(Ticket ticket) {
        long id = (ticket.id() == null) ? lastId.incrementAndGet() : ticket.id();
        Ticket saved = new Ticket(id, ticket.title(), ticket.priority(), ticket.status());
        tickets.put(id, saved);
        return saved;
    }

    @Override
    public Optional<Ticket> findById(long id) {
        return Optional.ofNullable(tickets.get(id));
    }
}
