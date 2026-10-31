package com.bsp.supportassistant.exception;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(long ticketId) {
        super("Ticket " + ticketId + " does not exist");
    }
}
