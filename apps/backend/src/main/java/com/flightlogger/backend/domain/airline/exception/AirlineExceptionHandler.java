package com.flightlogger.backend.domain.airline.exception;

import com.flightlogger.backend.domain.airline.controller.AirlineController;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = AirlineController.class)
public class AirlineExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleConstraintViolationException() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "invalid ICAO code");
    }

    @ExceptionHandler(AirlineNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleAirlineNotFoundException(AirlineNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
