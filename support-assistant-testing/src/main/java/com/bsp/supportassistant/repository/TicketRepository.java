package com.bsp.supportassistant.repository;

import com.bsp.supportassistant.model.Ticket;

import java.util.Optional;

// What the service needs from a store of tickets. It is an interface,
// so a test can replace it with a Mockito mock.
public interface TicketRepository {

    // Saves the ticket and gives it back with its id.
    Ticket save(Ticket ticket);

    Optional<Ticket> findById(long id);
}
