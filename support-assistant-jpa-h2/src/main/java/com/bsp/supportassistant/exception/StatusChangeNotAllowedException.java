package com.bsp.supportassistant.exception;

// The answer is 409: the ticket is not in a state that allows this change.
public class StatusChangeNotAllowedException extends RuntimeException {

    public StatusChangeNotAllowedException(String message) {
        super(message);
    }
}
