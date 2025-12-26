package com.flightlogger.backend.testdata;

import com.flightlogger.backend.model.AirlineReadDto;

public class AirlineTestData {

    public static final AirlineReadDto DLH_READ_DTO = new AirlineReadDto(
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
}
