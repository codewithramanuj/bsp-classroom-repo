package com.bsp.supportassistantplsql.api;

import com.bsp.supportassistantplsql.dto.CreateTicketRequest;
import com.bsp.supportassistantplsql.dto.TicketCreated;
import com.bsp.supportassistantplsql.dto.TicketResponse;
import com.bsp.supportassistantplsql.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public List<TicketResponse> findTickets(@RequestParam(required = false) String status,
    @RequestParam(required = false) String priority){
      return ticketService.findTickets(status);
    }

//    @GetMapping
//    public List<TicketResponse> findTickets(){
//        return ticketService.findTickets(null);
//    }
//
//    @GetMapping("/status/{status}")
//    public List<TicketResponse> findTickets(@PathVariable String status){
//        return ticketService.findTickets(status);
//    }
//
//    @GetMapping("/priority/{priority}")
//    public List<TicketResponse> findTicketsByPriority(@PathVariable String priority){
//        return null;
//    }

    //    @GetMapping("status/{status}/priority/{priority}")
//    public List<TicketResponse> findTicketsByPriority(@PathVariable String priority){
//        return null;
//    }

}
