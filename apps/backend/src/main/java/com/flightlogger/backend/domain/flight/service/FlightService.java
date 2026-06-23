package com.flightlogger.backend.domain.flight.service;

import com.flightlogger.backend.model.FlightReadDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface FlightService {

    Page<FlightReadDto> getAllFlights(int page, int size);

    FlightReadDto getFlightById(UUID flightId);
}
