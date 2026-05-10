package com.flightlogger.backend.domain.airport.service;

import com.flightlogger.backend.model.AirportCreateDto;
import com.flightlogger.backend.model.AirportReadDto;
import com.flightlogger.backend.model.AirportUpdateDto;

import java.util.List;

public interface AirportService {
    List<AirportReadDto> getAllAirports();

    AirportReadDto getAirportByIcao(String airportIcao);

    AirportReadDto saveAirport(AirportCreateDto dto);

    AirportReadDto updateAirportByIcao(String icao, AirportUpdateDto dto);

    void deleteAirportByIcao(String airportIcao);
}
