package com.flightlogger.backend.domain.flight.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface FlightRepository extends JpaRepository<Flight, UUID> {

    @NonNull
    @EntityGraph(attributePaths = {"origin", "origin.country", "destination", "destination.country", "airline"})
    Page<Flight> findAll(@NonNull Pageable pageable);
}
