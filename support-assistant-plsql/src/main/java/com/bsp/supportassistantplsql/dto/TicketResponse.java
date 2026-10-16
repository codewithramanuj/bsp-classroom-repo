package com.bsp.supportassistantplsql.dto;

import java.time.LocalDateTime;

public record TicketResponse(
        long id,
        String title,
        String description,
        String status,
        String priority,
        LocalDateTime createdAt,
        LocalDateTime dueBy) {
}
