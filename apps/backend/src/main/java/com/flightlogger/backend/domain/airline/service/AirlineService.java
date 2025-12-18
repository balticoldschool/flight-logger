package com.flightlogger.backend.domain.airline.service;

import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import com.flightlogger.backend.model.AirlineUpdateDto;

import java.util.List;

public interface AirlineService {
    List<Airline> getAllAirlines();

    Airline getAirlineByIcao(String airlineIcao);

    AirlineReadDto saveAirline(AirlineCreateDto dto);

    AirlineReadDto updateAirline(String icao, AirlineUpdateDto dto);

    void deleteAirline(String airlineIcao);
}
