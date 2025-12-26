package com.flightlogger.backend.domain.airline.controller;

import com.flightlogger.backend.api.AirlinesApi;
import com.flightlogger.backend.domain.airline.service.AirlineService;
import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import com.flightlogger.backend.model.AirlineUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AirlineController implements AirlinesApi {

   private final AirlineService airlineService;

    @Override
    public ResponseEntity<AirlineReadDto> createAirline(AirlineCreateDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(airlineService.saveAirline(dto));
    }

    @Override
    public ResponseEntity<Void> deleteAirlineByIcao(String airlineIcao) {
        airlineService.deleteAirline(airlineIcao);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AirlineReadDto> getAirlineByIcao(String airlineIcao) {
        return ResponseEntity.ok().body(airlineService.getAirlineByIcao(airlineIcao.toUpperCase()));
    }

    @Override
    public ResponseEntity<List<AirlineReadDto>> getAllAirlines() {

        return ResponseEntity.ok().body(airlineService.getAllAirlines());
    }

    @Override
    public ResponseEntity<AirlineReadDto> updateAirlineByIcao(String airlineIcao, AirlineUpdateDto airlineUpdateDto) {
        return ResponseEntity.ok().body(airlineService.updateAirline(airlineIcao, airlineUpdateDto));
    }
}
