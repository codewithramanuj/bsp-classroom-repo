package com.bsp.supportassistant.mapper;

import com.bsp.supportassistant.dto.TicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.model.Ticket;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TicketMapper {

    public  Ticket toEntity(TicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setStatus(request.status());
        return ticket;
    }

    public  TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority());
    }

    public  List<TicketResponse> toResponseList(List<Ticket> tickets) {
        List<TicketResponse> responses = new ArrayList<>();
        for (Ticket ticket : tickets) {
            responses.add(toResponse(ticket));
        }
        return responses;
    }
}
