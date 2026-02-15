package com.flightlogger.backend.domain.airport.exception;

public class AirportNotFoundException extends RuntimeException {
    public AirportNotFoundException(String icaoCode) {
        super(String.format("Airport with ICAO code %s not found", icaoCode));
    }
}
