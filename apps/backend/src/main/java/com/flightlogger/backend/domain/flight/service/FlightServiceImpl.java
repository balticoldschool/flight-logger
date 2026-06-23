package com.flightlogger.backend.domain.flight.service;

import com.flightlogger.backend.domain.flight.entity.FlightMapper;
import com.flightlogger.backend.domain.flight.entity.FlightRepository;
import com.flightlogger.backend.domain.flight.exception.FlightNotFoundException;
import com.flightlogger.backend.model.FlightReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    @Override
    public Page<FlightReadDto> getAllFlights(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("flightDate").ascending());

        return flightRepository.findAll(pageable).map(flightMapper::toDto);
    }

    @Override
    public FlightReadDto getFlightById(UUID flightId) {
        return flightRepository.findById(flightId)
                .map(flightMapper::toDto)
                .orElseThrow(FlightNotFoundException::new);
    }
}
