package com.flightlogger.backend.domain.airline.exception;

public class AirlineNotFoundException extends RuntimeException {
    public AirlineNotFoundException(String icaoCode) {
        super(String.format("Airline with ICAO code %s not found", icaoCode));
    }
}
