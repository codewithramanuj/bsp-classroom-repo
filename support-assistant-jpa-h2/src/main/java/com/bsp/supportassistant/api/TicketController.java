package com.bsp.supportassistant.api;

import com.bsp.supportassistant.dto.BulkStatusRequest;
import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.StatusRequest;
import com.bsp.supportassistant.dto.TicketCreated;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Calls the function OPEN_COUNT.
    @GetMapping("/open-count")
    public int openCount(@RequestParam String priority){
        return ticketService.openCount(priority);
    }

    // Calls the procedure FIND_TICKETS. Both filters are optional.
    @GetMapping
    public List<TicketResponse> findTickets(@RequestParam(required = false) String status,
    @RequestParam(required = false) String priority){
      return ticketService.findTickets(status, priority);
    }

    // Calls the procedure CREATE_TICKET.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCreated createTicket(@RequestBody CreateTicketRequest request){
        return new TicketCreated(ticketService.createTicket(request));
    }

    // Calls the procedure CHANGE_STATUS once.
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(@PathVariable long id, @RequestBody StatusRequest request){
        ticketService.changeStatus(id, request.status());
    }

    // Calls CHANGE_STATUS for each id, in one transaction.
    @PostMapping("/bulk-status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(@RequestBody BulkStatusRequest request){
        ticketService.changeStatus(request.ticketIds(), request.status());
    }

}
