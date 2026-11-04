package com.bsp.supportassistantplsql.exception;

// The request is wrong before any PL/SQL program is called.
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
