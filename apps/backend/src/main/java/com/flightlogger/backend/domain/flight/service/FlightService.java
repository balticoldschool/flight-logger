package com.flightlogger.backend.domain.flight.service;

import com.flightlogger.backend.model.FlightReadDto;
import org.springframework.data.domain.Page;

public interface FlightService {

    Page<FlightReadDto> getAllFlights(int page, int size);
}
