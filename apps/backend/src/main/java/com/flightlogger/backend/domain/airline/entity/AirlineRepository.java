package com.flightlogger.backend.domain.airline.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline, String> {
    boolean existsByIcaoCode(String icaoCode);

    boolean existsByIataCode(String iataCode);
}
