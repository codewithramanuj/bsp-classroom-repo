package com.bsp.supportassistantplsql.api;

import com.bsp.supportassistantplsql.dto.CreateTicketRequest;
import com.bsp.supportassistantplsql.dto.TicketCreated;
import com.bsp.supportassistantplsql.service.TicketService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    public TicketCreated createTicket(CreateTicketRequest request){
        return new TicketCreated(ticketService.createTicket(request));
    }

}
