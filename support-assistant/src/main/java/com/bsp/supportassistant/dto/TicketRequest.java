package com.bsp.supportassistant.dto;

import com.bsp.supportassistant.model.Priority;
import com.bsp.supportassistant.model.TicketStatus;

public record TicketRequest(String title, Priority priority, String description, TicketStatus status) { }
