package com.flightlogger.backend.testdata;

public class ErrorMessages {

    // Title
    public final static String BAD_REQUEST_ERROR_TITLE = "Bad Request";
    public final static String CONFLICT_ERROR_TITLE = "Conflict";
    public final static String NOT_FOUND_ERROR_TITLE = "Not Found";
    public final static String VALIDATION_ERROR_TITLE = "Validation error";

    // Messages
    public static final String AIRLINE_ICAO_ALREADY_EXISTS_MESSAGE = "Airline with ICAO %s already exists";
    public static final String AIRLINE_IATA_ALREADY_EXISTS_MESSAGE = "Airline with IATA %s already exists";
    public static final String AIRLINE_NOT_FOUND = "Airline with ICAO code %s not found";
    public final static String INVALID_IATA_CODE_MESSAGE = "Invalid IATA code";
    public final static String INVALID_ICAO_CODE_MESSAGE = "Invalid ICAO code";
    public final static String INVALID_NAME_MESSAGE = "Invalid Name field";
    public final static String MANDATORY_IATA_MISSING_MESSAGE = "Mandatory field IATA code missing";
    public final static String MANDATORY_ICAO_MISSING_MESSAGE = "Mandatory field ICAO code missing";
    public final static String MANDATORY_NAME_MISSING_MESSAGE = "Mandatory field name missing";
}
