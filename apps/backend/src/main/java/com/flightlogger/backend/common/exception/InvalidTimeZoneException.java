package com.flightlogger.backend.common.exception;

public class InvalidTimeZoneException extends RuntimeException {
    public InvalidTimeZoneException(String timezone) {
        super(String.format("Invalid timezone: %s", timezone));
    }
}
