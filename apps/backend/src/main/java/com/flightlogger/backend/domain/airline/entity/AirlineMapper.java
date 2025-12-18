package com.flightlogger.backend.domain.airline.entity;

import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AirlineMapper {

    @Mapping(target = "icao", source = "icaoCode")
    @Mapping(target = "iata", source = "iataCode")
    AirlineReadDto toDto(Airline airline);

    @Mapping(target = "icaoCode", source = "icao", qualifiedByName = "toUpperCase")
    @Mapping(target = "iataCode", source = "iata", qualifiedByName = "toUpperCase")
    Airline toEntity(AirlineCreateDto dto);

    @Named("toUpperCase")
    default String toUpperCase(String value) {
        return value != null ? value.trim().toUpperCase() : null;
    }
}
