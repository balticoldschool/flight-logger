package com.flightlogger.backend.domain.airline.controller;

import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.domain.airline.entity.AirlineRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/airlines")
public class AirlineController {

    private final AirlineRepository airlineRepository;

    @GetMapping
    public List<Airline> getAllAirlines() {
        return airlineRepository.findAll();
    }

}
