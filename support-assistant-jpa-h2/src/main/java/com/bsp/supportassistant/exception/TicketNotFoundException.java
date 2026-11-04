package com.bsp.supportassistant.exception;

// The answer is 404.
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(long id) {
        super("Ticket " + id + " does not exist");
    }
}
