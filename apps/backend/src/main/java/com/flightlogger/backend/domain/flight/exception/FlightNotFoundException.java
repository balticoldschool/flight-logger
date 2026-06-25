package com.flightlogger.backend.domain.flight.exception;

public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException() {
        super("Flight with given id not found");
    }
}
