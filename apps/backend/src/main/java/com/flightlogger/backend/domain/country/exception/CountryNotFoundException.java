package com.flightlogger.backend.domain.country.exception;

import java.util.UUID;

public class CountryNotFoundException extends RuntimeException {
    public CountryNotFoundException(UUID id) {
        super(String.format("Country with id %s does not exist", id));
    }
}
