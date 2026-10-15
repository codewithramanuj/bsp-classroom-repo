package com.bsp.supportassistant.api;

import com.bsp.supportassistant.dto.TicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.mapper.TicketMapper;
import com.bsp.supportassistant.model.Ticket;
import com.bsp.supportassistant.model.TicketStatus;
import com.bsp.supportassistant.repository.TicketRepository;
import org.hibernate.annotations.Audited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private TicketRepository ticketRepository ;
    @Autowired
    private TicketMapper ticketMapper;


    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // GET /api/v1/tickets lists every ticket.
    // GET /api/v1/tickets?status=OPEN lists only the tickets with that status.
    @GetMapping
    public List<TicketResponse> findAll(@RequestParam(required = false) TicketStatus status) {
        if (status == null) {
            return ticketMapper.toResponseList(ticketRepository.findAll());
        }
        return ticketMapper.toResponseList(ticketRepository.findAllByStatus(status));
    }

    @GetMapping("/{id}")
    public TicketResponse findById(@PathVariable Long id) {

        return ticketMapper.toResponse(ticketRepository.findById(id).orElseThrow(()->new TicketNotFoundException("Ticket with id " +id+ " not found")));

    }

    @PostMapping
    // @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TicketResponse> create(@RequestBody TicketRequest ticketRequest) {

        // Create ticket from ticket request
        Ticket ticket = ticketMapper.toEntity(ticketRequest);
        ticket.setStatus(TicketStatus.OPEN);
        // save the ticket
        Ticket saved = ticketRepository.save(ticket);

        // create ticket response from ticket
        TicketResponse response = ticketMapper.toResponse(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    // Send only the fields you want to change.
    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable Long id, @RequestBody TicketRequest ticketRequest) {
        // find the ticket by id
        Ticket ticket = ticketRepository.findById(id).get();
        // make changes with the setter methods
        if(ticketRequest.description()!=null){
            ticket.setDescription(ticketRequest.description());
        }
        if(ticketRequest.status()!=null){
            ticket.setStatus(ticketRequest.status());
        }
        if(ticketRequest.title()!=null){
            ticket.setTitle(ticketRequest.title());
        }
        // save the ticket back
        return  ticketMapper.toResponse(ticketRepository.save(ticket));

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ticketRepository.deleteById(id);
    }
}
