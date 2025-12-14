package com.flightlogger.backend.domain.airline.service;

import com.flightlogger.backend.domain.airline.entity.Airline;

import java.util.List;

public interface AirlineService {
    List<Airline> getAllAirlines();

    Airline getAirlineByIcao(String airlineIcao);
}
