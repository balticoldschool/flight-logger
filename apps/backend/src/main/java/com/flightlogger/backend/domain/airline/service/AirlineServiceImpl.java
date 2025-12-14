package com.flightlogger.backend.domain.airline.service;

import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.domain.airline.entity.AirlineRepository;
import com.flightlogger.backend.domain.airline.exception.AirlineNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository airlineRepository;

    public List<Airline> getAllAirlines(){
        return airlineRepository.findAll();
    }

    @Override
    public Airline getAirlineByIcao(String airlineIcao) {
        return airlineRepository.findById(airlineIcao).orElseThrow(() -> new AirlineNotFoundException(airlineIcao));
    }
}
