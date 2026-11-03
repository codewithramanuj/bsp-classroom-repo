package com.bsp.springsecurityexample.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    @PreAuthorize("hasRole('ADMIN')")
    public String assignTicket(){
        return "Ticket assigned..";
    }


}
