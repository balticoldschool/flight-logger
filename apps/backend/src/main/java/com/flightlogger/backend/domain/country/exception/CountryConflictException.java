package com.flightlogger.backend.domain.country.exception;

public class CountryConflictException extends RuntimeException {
    public CountryConflictException() {
        super("Country still used in a foreign key relation");
    }
}
