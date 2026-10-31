package com.bsp.supportassistant.service;

import com.bsp.supportassistant.dto.CommentRequest;
import com.bsp.supportassistant.dto.CommentResponse;
import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.dto.TicketWithComments;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.model.Comment;
import com.bsp.supportassistant.model.Ticket;
import com.bsp.supportassistant.repository.TicketPackageRepository;
import com.bsp.supportassistant.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Two ways to reach the database, and a simple rule for choosing:
//   changes follow rules      -> the PL/SQL package (ticketPackage)
//   reads and plain look-ups  -> JPA (ticketRepository)
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketPackageRepository ticketPackage;

    public TicketService(TicketRepository ticketRepository, TicketPackageRepository ticketPackage) {
        this.ticketRepository = ticketRepository;
        this.ticketPackage = ticketPackage;
    }

    // ---- Changes: through the package ----

    public long create(CreateTicketRequest request) {
        return ticketPackage.createTicket(request.title(), request.description(), request.priority());
    }

    public long addComment(long ticketId, CommentRequest request) {
        return ticketPackage.addComment(ticketId, request.author(), request.body());
    }

    // Both ways in one transaction: the package changes the status, then JPA reads the result.
    // The read comes after the change, so JPA sees the new status.
    @Transactional
    public TicketResponse changeStatus(long ticketId, String newStatus) {
        ticketPackage.changeStatus(ticketId, newStatus);
        return toResponse(findTicket(ticketId));
    }

    // ---- Reads: through JPA ----

    @Transactional(readOnly = true)
    public List<TicketResponse> find(String status) {
        List<Ticket> tickets = (status == null)
                ? ticketRepository.findAllByOrderById()
                : ticketRepository.findByStatusOrderById(status);
        return tickets.stream().map(this::toResponse).toList();
    }

    // The comments are read here, inside the transaction, when getComments() is called.
    @Transactional(readOnly = true)
    public TicketWithComments get(long ticketId) {
        Ticket ticket = findTicket(ticketId);
        List<CommentResponse> comments = ticket.getComments().stream().map(this::toResponse).toList();
        return new TicketWithComments(toResponse(ticket), comments);
    }

    private Ticket findTicket(long ticketId) {
        return ticketRepository.findById(ticketId).orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    // Entities never leave the service. The controller only sees records.
    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getTitle(), ticket.getDescription(),
                ticket.getStatus(), ticket.getPriority(), ticket.getCreatedAt(), ticket.getDueBy());
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getAuthor(), comment.getBody(), comment.getCreatedAt());
    }
}
