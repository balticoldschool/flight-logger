package com.flightlogger.backend.domain.airline.service;

import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import com.flightlogger.backend.model.AirlineUpdateDto;

import java.util.List;

public interface AirlineService {
    List<AirlineReadDto> getAllAirlines();

    AirlineReadDto getAirlineByIcao(String airlineIcao);

    AirlineReadDto saveAirline(AirlineCreateDto dto);

    AirlineReadDto updateAirline(String icao, AirlineUpdateDto dto);

    void deleteAirline(String airlineIcao);
}
