package com.flightlogger.backend.domain.airport.service;

import com.flightlogger.backend.model.AirportReadDto;

import java.util.List;

public interface AirportService {
    List<AirportReadDto> getAllAirports();

    AirportReadDto getAirportByIcao(String airportIcao);
}
