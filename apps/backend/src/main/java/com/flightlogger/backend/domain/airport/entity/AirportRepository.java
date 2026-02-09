package com.flightlogger.backend.domain.airport.entity;

import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AirportRepository extends JpaRepository<Airport, String> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"country"})
    List<Airport> findAll();
}
