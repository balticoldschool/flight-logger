package com.flightlogger.backend.domain.flight.exception;

public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException() {
        super("flight with given id not found");
    }
}
