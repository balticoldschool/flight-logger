package com.flightlogger.backend.testdata;

import com.flightlogger.backend.domain.airport.entity.Airport;
import com.flightlogger.backend.model.AirportReadDto;

import static com.flightlogger.backend.testdata.CountryTestData.GERMANY_COUNTRY;
import static com.flightlogger.backend.testdata.CountryTestData.GERMANY_READ_DTO;

public class AirportTestData {

    public static final Airport FRANKFURT_AIRPORT = new Airport(
            "EDDF",
            "FRA",
            "Frankfurt am Main Airport",
            "Frankfurt",
            GERMANY_COUNTRY,
            "Europe/Berlin"
    );

    public static AirportReadDto FRANKFURT_AIRPORT_READ_DTO = new AirportReadDto()
            .icao(FRANKFURT_AIRPORT.getIcaoCode())
            .iata(FRANKFURT_AIRPORT.getIataCode())
            .name(FRANKFURT_AIRPORT.getName())
            .city(FRANKFURT_AIRPORT.getCity())
            .country(GERMANY_READ_DTO)
            .timezone(FRANKFURT_AIRPORT.getTimezone());

    public static AirportReadDto MUNICH_AIRPORT_READ_DTO = new AirportReadDto()
            .icao("EDDM")
            .iata("MUC")
            .name("Flughafen München Franz Josef Strauss")
            .city("Munich")
            .country(GERMANY_READ_DTO)
            .timezone("Europe/Berlin");
}
