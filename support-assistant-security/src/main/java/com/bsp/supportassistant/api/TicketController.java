package com.bsp.supportassistant.api;

import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.StatusRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// The tickets are fixed examples. This project is about WHO may call each URL, not about storing tickets.
// The rules for who may call are in config/SecurityConfig, not here.
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @GetMapping
    public List<TicketResponse> list() {
        return List.of(
                new TicketResponse(1, "VPN drops every hour", "OPEN"),
                new TicketResponse(2, "Printer is jammed", "IN_PROGRESS"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@RequestBody CreateTicketRequest request) {
        return new TicketResponse(3, request.title(), "OPEN");
    }

    @PatchMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable long id, @RequestBody StatusRequest request) {
        return new TicketResponse(id, "VPN drops every hour", request.status());
    }
}
