package com.bsp.supportassistantplsql.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public record TicketResponse(
        long id,
        String title,
        String description,
        String status,
        String priority,
        LocalDateTime createdAt,
        LocalDateTime dueBy) {



}
