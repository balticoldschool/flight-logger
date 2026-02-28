package com.flightlogger.backend.domain.airport.exception;

import com.flightlogger.backend.common.utils.IdentificationCodeTypes;

public class AirportAlreadyExistsException extends RuntimeException {
    public AirportAlreadyExistsException(IdentificationCodeTypes attribut, String airportCode) {
        super(String.format("Airport with %s code %s already exists", attribut.name(), airportCode));
    }
}
