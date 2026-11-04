package com.bsp.supportassistant.dto;

import java.util.List;

// The body of POST /api/v1/tickets/bulk-status.
public record BulkStatusRequest(List<Long> ticketIds, String status) {
}
