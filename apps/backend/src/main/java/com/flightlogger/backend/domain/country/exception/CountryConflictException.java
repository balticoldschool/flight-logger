package com.flightlogger.backend.domain.country.exception;

import java.util.UUID;

public class CountryConflictException extends RuntimeException {
    public CountryConflictException(UUID id) {
        super(String.format("Country with id %s still used in a foreign key relation", id));
    }
}
