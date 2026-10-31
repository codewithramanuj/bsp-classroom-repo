package com.bsp.supportassistant.exception;

public class InvalidStatusChangeException extends RuntimeException {

    public InvalidStatusChangeException(String from, String to) {
        super("A ticket cannot move from " + from + " to " + to);
    }
}
