package com.flightlogger.backend.domain.airline.entity;

import com.flightlogger.backend.common.utils.StringFormatter;
import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import com.flightlogger.backend.model.AirlineUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = StringFormatter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AirlineMapper {

    @Mapping(target = "icao", source = "icaoCode")
    @Mapping(target = "iata", source = "iataCode")
    AirlineReadDto toDto(Airline airline);

    @Mapping(target = "icaoCode", source = "icao", qualifiedByName = "toUpperCase")
    @Mapping(target = "iataCode", source = "iata", qualifiedByName = "toUpperCase")
    Airline toEntity(AirlineCreateDto dto);

    @Mapping(target = "iataCode", source = "iata", qualifiedByName = "toUpperCase")
    void updateFromDto(AirlineUpdateDto dto, @MappingTarget Airline airline);
}
