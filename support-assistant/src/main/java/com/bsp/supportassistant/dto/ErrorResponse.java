package com.bsp.supportassistant.dto;

import java.time.LocalDateTime;

public record ErrorResponse(String message,
                            LocalDateTime timestamp,
                            String status,
                            String path) {
}
