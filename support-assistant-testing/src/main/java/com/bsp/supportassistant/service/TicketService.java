package com.bsp.supportassistant.service;

import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.exception.InvalidStatusChangeException;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.model.StatusRules;
import com.bsp.supportassistant.model.Ticket;
import com.bsp.supportassistant.repository.TicketRepository;
import org.springframework.stereotype.Service;

// The business rules. This is the class that the Mockito tests look at.
@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // A new ticket always starts as OPEN. The title is required.
    public TicketResponse create(CreateTicketRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        Ticket saved = ticketRepository.save(new Ticket(null, request.title(), request.priority(), "OPEN"));
        return toResponse(saved);
    }

    public TicketResponse get(long id) {
        return toResponse(find(id));
    }

    // A ticket moves one step forward at a time.
    public TicketResponse changeStatus(long id, String newStatus) {
        Ticket ticket = find(id);
        if (!StatusRules.isAllowed(ticket.status(), newStatus)) {
            throw new InvalidStatusChangeException(ticket.status(), newStatus);
        }
        return toResponse(ticketRepository.save(ticket.withStatus(newStatus)));
    }

    private Ticket find(long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(ticket.id(), ticket.title(), ticket.priority(), ticket.status());
    }
}
