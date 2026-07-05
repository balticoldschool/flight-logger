package com.flightlogger.backend.domain.flight.exception;

public class DuplicateFlightNumberException extends RuntimeException {
    public DuplicateFlightNumberException() {
        super("A flight with the same airline and flight number already exists on that date");
    }
}