package com.flightlogger.backend.testdata;

import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;

public class AirlineTestData {

    public static final AirlineReadDto DLH_READ_DTO = new AirlineReadDto(
            "DLH",
            "LH",
            "Deutsche Lufthansa",
            "www.logo.foo"
    );

    public static final Airline DLH_AIRLINE = new Airline(
            "DLH",
            "LH",
            "Deutsche Lufthansa",
            "www.logo.foo"
    );

    public static final AirlineReadDto CFG_READ_DTO = new AirlineReadDto(
            "CFG",
            "DE",
            "Condor",
            "www.logo.bar"
    );

    public static final Airline CFG_AIRLINE = new Airline(
            "CFG",
            "DE",
            "Condor",
            "www.logo.bar"
    );

    public static final AirlineCreateDto CFG_CREATE_DTO = new AirlineCreateDto(
            "cfg",
            "de",
            "Condor"
    );
}
