package com.bsp.supportassistant.service;

import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.exception.InvalidRequestException;
import com.bsp.supportassistant.exception.StatusChangeNotAllowedException;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.mapper.TicketMapper;
import com.bsp.supportassistant.model.Ticket;
import com.bsp.supportassistant.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// In this project there is no PL/SQL package, so the business rules are written here.
// They are the same rules that SUPPORT_TICKET_PKG has, with the same messages.
@Service
public class TicketService {

    // The service level rule: HIGH is due in 1 day, MEDIUM in 3 days, LOW in 7 days.
    private static final Map<String, Integer> DAYS_TO_RESOLVE = Map.of("HIGH", 1, "MEDIUM", 3, "LOW", 7);

    // The life of a ticket: OPEN, IN_PROGRESS, RESOLVED, CLOSED. One step at a time.
    private static final Map<String, String> NEXT_STATUS =
            Map.of("OPEN", "IN_PROGRESS", "IN_PROGRESS", "RESOLVED", "RESOLVED", "CLOSED");

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketService(TicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    // How many tickets of this priority are not finished.
    public int openCount(String priority) {
        return (int) ticketRepository.countByPriorityAndStatusIn(priority, List.of("OPEN", "IN_PROGRESS"));
    }

    public long createTicket(CreateTicketRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidRequestException("Title is required");
        }
        if (request.title().length() > 100) {
            throw new InvalidRequestException("Title must have 100 characters or fewer");
        }
        if (request.description() != null && request.description().length() > 500) {
            throw new InvalidRequestException("Description must have 500 characters or fewer");
        }
        Integer days = DAYS_TO_RESOLVE.get(request.priority());
        if (days == null) {
            throw new InvalidRequestException("Priority must be HIGH, MEDIUM or LOW");
        }

        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = new Ticket(request.title(), request.description(), request.priority(),
                now, now.plusDays(days));
        return ticketRepository.save(ticket).getId();
    }

    // Both filters are optional.
    public List<TicketResponse> findTickets(String status, String priority) {
        List<Ticket> tickets = ticketRepository.search(upperCase(status), upperCase(priority));
        return ticketMapper.toResponseList(tickets);
    }

    @Transactional
    public void changeStatus(long ticketId, String newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        String next = NEXT_STATUS.get(ticket.getStatus());
        String wanted = upperCase(newStatus);

        if (next == null) {
            throw new StatusChangeNotAllowedException("Ticket " + ticketId + " is CLOSED and cannot change");
        }
        if (!next.equals(wanted)) {
            throw new StatusChangeNotAllowedException("Ticket " + ticketId + " is " + ticket.getStatus()
                    + ". The next status is " + next);
        }
        // The ticket is a managed entity: the change is saved when the transaction ends.
        ticket.setStatus(wanted);
    }

    // All or nothing: if one ticket cannot change, the tickets before it are put back.
    @Transactional
    public void changeStatus(List<Long> ticketIds, String newStatus) {
        if (ticketIds == null || ticketIds.isEmpty()) {
            throw new InvalidRequestException("ticketIds must hold at least one id");
        }
        for (Long ticketId : ticketIds) {
            changeStatus(ticketId, newStatus);
        }
    }

    private String upperCase(String value) {
        return value == null ? null : value.toUpperCase();
    }

    public void deleteTicket(long id){
        ticketRepository.deleteById(id);
    }
}
