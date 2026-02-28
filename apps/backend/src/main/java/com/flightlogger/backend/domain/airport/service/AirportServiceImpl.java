package com.flightlogger.backend.domain.airport.service;

import com.flightlogger.backend.domain.airport.entity.Airport;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.domain.airport.entity.AirportRepository;
import com.flightlogger.backend.domain.airport.exception.AirportNotFoundException;
import com.flightlogger.backend.model.AirportReadDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AirportServiceImpl implements AirportService {

    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AirportReadDto> getAllAirports() {
        List<Airport> airports = airportRepository.findAll();
        return airports.stream().map(airportMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AirportReadDto getAirportByIcao(String airportIcao) {
        return airportRepository.findById(airportIcao)
                .map(airportMapper::toDto)
                .orElseThrow(() -> new AirportNotFoundException(airportIcao));
    }
}
