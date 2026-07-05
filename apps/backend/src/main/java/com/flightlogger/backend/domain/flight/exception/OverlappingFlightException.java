package com.flightlogger.backend.domain.flight.exception;

public class OverlappingFlightException extends RuntimeException {
    public OverlappingFlightException() {
        super("A flight already exists that overlaps the given time window");
    }
}