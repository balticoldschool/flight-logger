package com.flightlogger.backend.domain.airline.controller;

import com.flightlogger.backend.api.AirlinesApi;
import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.domain.airline.entity.AirlineMapper;
import com.flightlogger.backend.domain.airline.service.AirlineService;
import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AirlineController implements AirlinesApi {

   private final AirlineService airlineService;
   private final AirlineMapper airlineMapper;

    @Override
    public ResponseEntity<AirlineReadDto> createAirline(AirlineCreateDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(airlineService.saveAirline(dto));
    }

    @Override
    public ResponseEntity<AirlineReadDto> getAirlineByIcao(String airlineIcao) {
        final Airline airline = airlineService.getAirlineByIcao(airlineIcao.toUpperCase());

        return ResponseEntity.ok().body(airlineMapper.toDto(airline));
    }

    @Override
    public ResponseEntity<List<AirlineReadDto>> getAllAirlines() {
        final List<Airline> airlines = airlineService.getAllAirlines();

        return ResponseEntity.ok().body(
                airlines.stream()
                        .map(airlineMapper::toDto)
                        .toList());
    }
}
