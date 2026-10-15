package com.bsp.supportassistant.exception;

import com.bsp.supportassistant.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class TicketErrorController {

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTicketNotFoundError(TicketNotFoundException e,
                                                   HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.toString(),
                request.getRequestURI()
        );
        return response;
    }

}
