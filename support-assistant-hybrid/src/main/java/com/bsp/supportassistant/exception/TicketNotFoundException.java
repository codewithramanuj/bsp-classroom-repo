package com.bsp.supportassistant.exception;

// JPA found no ticket with this id. The package has its own version of this rule (error 20003).
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(long ticketId) {
        super("Ticket " + ticketId + " does not exist");
    }
}
