package com.flightlogger.backend.domain.airline.service;

import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;

import java.util.List;

public interface AirlineService {
    List<Airline> getAllAirlines();

    Airline getAirlineByIcao(String airlineIcao);

    AirlineReadDto saveAirline(AirlineCreateDto dto);

    void deleteAirline(String airlineIcao);
}
