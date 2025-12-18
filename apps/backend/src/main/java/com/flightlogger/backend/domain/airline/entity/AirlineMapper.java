package com.flightlogger.backend.domain.airline.entity;

import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import com.flightlogger.backend.model.AirlineUpdateDto;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AirlineMapper {

    @Mapping(target = "icao", source = "icaoCode")
    @Mapping(target = "iata", source = "iataCode")
    AirlineReadDto toDto(Airline airline);

    @Mapping(target = "icaoCode", source = "icao", qualifiedByName = "toUpperCase")
    @Mapping(target = "iataCode", source = "iata", qualifiedByName = "toUpperCase")
    Airline toEntity(AirlineCreateDto dto);

    @Mapping(target = "iataCode", source = "iata", qualifiedByName = "toUpperCase")
    void updateFromDto(AirlineUpdateDto dto, @MappingTarget Airline airline);

    @Named("toUpperCase")
    default String toUpperCase(String value) {
        return value != null ? StringUtils.upperCase(value) : null;
    }
}
