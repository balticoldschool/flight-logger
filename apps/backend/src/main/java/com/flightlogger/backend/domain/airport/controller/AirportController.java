package com.flightlogger.backend.domain.airport.controller;

import com.flightlogger.backend.api.AirportsApi;
import com.flightlogger.backend.domain.airport.service.AirportService;
import com.flightlogger.backend.model.AirportReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AirportController implements AirportsApi {

    private final AirportService airportService;

    @Override
    public ResponseEntity<AirportReadDto> getAirportByIcao(String icao) {
        return ResponseEntity.ok().body(airportService.getAirportByIcao(icao.toUpperCase()));
    }

    @Override
    public ResponseEntity<List<AirportReadDto>> getAllAirports() {
        return ResponseEntity.ok().body(airportService.getAllAirports());
    }
}
