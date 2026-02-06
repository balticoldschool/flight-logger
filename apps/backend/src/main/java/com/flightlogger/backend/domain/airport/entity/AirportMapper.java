package com.flightlogger.backend.domain.airport.entity;

import com.flightlogger.backend.common.utils.StringFormatter;
import com.flightlogger.backend.domain.country.entity.CountryMapper;
import com.flightlogger.backend.model.AirportReadDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = { StringFormatter.class, CountryMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AirportMapper {

    @Mapping(source = "icaoCode", target = "icao")
    @Mapping(source = "iataCode", target = "iata")
    AirportReadDto toDto(Airport airport);
}
