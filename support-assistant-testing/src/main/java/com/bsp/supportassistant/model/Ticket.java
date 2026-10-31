package com.bsp.supportassistant.model;

// A ticket. The id is null until the repository saves it.
public record Ticket(Long id, String title, String priority, String status) {

    // A record cannot be changed, so a change makes a new ticket with the new status.
    public Ticket withStatus(String newStatus) {
        return new Ticket(id, title, priority, newStatus);
    }
}
