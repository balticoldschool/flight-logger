package com.flightlogger.backend.domain.country.exception;

public class CountryAlreadyExistsException extends RuntimeException {
    public CountryAlreadyExistsException(String isoCode) {
        super(String.format("Country with ISO code %s already exists", isoCode));
    }
}
