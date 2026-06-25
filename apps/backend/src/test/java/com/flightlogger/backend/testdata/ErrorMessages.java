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
    public static final String AIRLINE_NOT_FOUND_MESSAGE = "Airline with ICAO code %s not found";
    public static final String AIRPORT_ALREADY_EXISTS = "Airport with %s code %s already exists";
    public static final String AIRPORT_NOT_FOUND_MESSAGE = "Airport with ICAO code %s not found";
    public static final String COUNTRY_ALREADY_EXISTS = "Country with ISO code %s already exists";
    public static final String COUNTRY_CONFLICT_MESSAGE = "Country still used in a foreign key relation";
    public static final String COUNTRY_NOT_FOUND_MESSAGE = "Country with id %s does not exist";
    public static final String FLIGHTS_NOT_FOUND_MESSAGE = "Flight with given id not found";
    public final static String INVALID_CITY_CODE_MESSAGE = "Invalid City";
    public final static String INVALID_IATA_CODE_MESSAGE = "Invalid IATA code";
    public final static String INVALID_ICAO_CODE_MESSAGE = "Invalid ICAO code";
    public final static String INVALID_ID_MESSAGE = "Invalid ID";
    public final static String INVALID_ISO2_MESSAGE = "Invalid ISO-2 code";
    public final static String INVALID_ISO3_MESSAGE = "Invalid ISO-3 code";
    public final static String INVALID_NAME_MESSAGE = "Invalid Name field";
    public final static String INVALID_PAGE_NUMBER_MESSAGE = "Invalid page number";
    public final static String INVALID_PAGE_SIZE_MESSAGE = "Invalid page size";
    public final static String INVALID_TIMEZONE_MESSAGE = "Invalid timezone: %s";

    public final static String MANDATORY_FIELD_MISSING = "Mandatory field %s is missing";
    public final static String MANDATORY_CITY_MISSING_MESSAGE = String.format(MANDATORY_FIELD_MISSING, "city");
    public final static String MANDATORY_COUNTRYID_MISSING_MESSAGE = String.format(MANDATORY_FIELD_MISSING, "countryId");
    public final static String MANDATORY_FLAGEMOJI_MISSING_MESSAGE = String.format(MANDATORY_FIELD_MISSING, "flagEmoji");
    public final static String MANDATORY_IATA_MISSING_MESSAGE = String.format(MANDATORY_FIELD_MISSING, "iata");
    public final static String MANDATORY_ICAO_MISSING_MESSAGE = String.format(MANDATORY_FIELD_MISSING, "icao");
    public final static String MANDATORY_ISO2_MISSING_MESSAGE = String.format(MANDATORY_FIELD_MISSING, "isoCode2");
    public final static String MANDATORY_ISO3_MISSING_MESSAGE = String.format(MANDATORY_FIELD_MISSING, "isoCode3");
    public final static String MANDATORY_NAME_MISSING_MESSAGE = String.format(MANDATORY_FIELD_MISSING, "name");
    public final static String MANDATORY_TIMEZONE_MISSING_MESSAGE = String.format(MANDATORY_FIELD_MISSING, "timezone");
}
