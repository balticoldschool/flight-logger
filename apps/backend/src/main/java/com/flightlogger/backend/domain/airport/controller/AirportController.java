package com.flightlogger.backend.domain.airport.controller;

import com.flightlogger.backend.api.AirportsApi;
import com.flightlogger.backend.domain.airport.service.AirportService;
import com.flightlogger.backend.model.AirportCreateDto;
import com.flightlogger.backend.model.AirportReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class AirportController implements AirportsApi {

    private final AirportService airportService;

    @Override
    public ResponseEntity<AirportReadDto> createAirport(AirportCreateDto airportCreateDto) {
        final AirportReadDto savedAirport = airportService.saveAirport(airportCreateDto);

        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{icao}")
                .buildAndExpand(savedAirport.getIcao())
                .toUri();

        return ResponseEntity.created(location).body(savedAirport);
    }

    @Override
    public ResponseEntity<Void> deleteAirportByIcao(String icao) {
        airportService.deleteAirport(icao.toUpperCase());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AirportReadDto> getAirportByIcao(String icao) {
        return ResponseEntity.ok().body(airportService.getAirportByIcao(icao.toUpperCase()));
    }

    @Override
    public ResponseEntity<List<AirportReadDto>> getAllAirports() {
        return ResponseEntity.ok().body(airportService.getAllAirports());
    }
}
