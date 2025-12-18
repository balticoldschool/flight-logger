package com.flightlogger.backend.domain.airline.exception;

public class AirlineAlreadyExistsException extends RuntimeException {
    public AirlineAlreadyExistsException(String attribute, String value) {

        super(String.format("Airline with %s %s already exists", attribute, value));
    }
}
