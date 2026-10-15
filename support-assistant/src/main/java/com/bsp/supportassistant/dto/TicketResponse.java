package com.bsp.supportassistant.dto;

import com.bsp.supportassistant.model.Priority;
import com.bsp.supportassistant.model.TicketStatus;

public record TicketResponse(Long id,  String title, String description
        ,TicketStatus status, Priority priority) { }
