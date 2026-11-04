package com.bsp.supportassistantplsql.exception;

import java.time.LocalDateTime;

// The body of every error response.
public record ApiError(
        int status,
        String error,
        String message,
        LocalDateTime timestamp) {
}
