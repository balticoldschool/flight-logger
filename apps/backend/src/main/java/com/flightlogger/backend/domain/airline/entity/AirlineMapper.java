package com.flightlogger.backend.domain.airline.entity;

import com.flightlogger.backend.model.AirlineReadDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AirlineMapper {

    @Mapping(target = "icao", source = "icaoCode")
    @Mapping(target = "iata", source = "iataCode")
    AirlineReadDto toDto(Airline airline);
}
