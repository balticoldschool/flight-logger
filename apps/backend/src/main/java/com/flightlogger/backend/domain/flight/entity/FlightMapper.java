package com.flightlogger.backend.domain.flight.entity;

import com.flightlogger.backend.domain.airline.entity.AirlineMapper;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.model.FlightReadDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {AirportMapper.class, AirlineMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlightMapper {

    FlightReadDto toDto(Flight flight);
}
