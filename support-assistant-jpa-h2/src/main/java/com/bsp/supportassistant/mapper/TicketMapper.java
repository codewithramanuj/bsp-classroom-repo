package com.bsp.supportassistant.mapper;

import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.model.Ticket;
import org.springframework.stereotype.Component;

import java.util.List;

// Entities never leave the service. The controller only sees TicketResponse.
@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),
                ticket.getDueBy());
    }

    public List<TicketResponse> toResponseList(List<Ticket> tickets) {
        return tickets.stream().map(this::toResponse).toList();
    }
}
