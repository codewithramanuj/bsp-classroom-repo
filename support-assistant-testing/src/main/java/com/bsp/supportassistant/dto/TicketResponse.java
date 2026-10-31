package com.bsp.supportassistant.dto;

// What the API sends back for a ticket.
public record TicketResponse(long id, String title, String priority, String status) {
}
