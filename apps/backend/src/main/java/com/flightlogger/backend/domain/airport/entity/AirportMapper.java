package com.flightlogger.backend.domain.airport.entity;

import com.flightlogger.backend.common.utils.StringFormatter;
import com.flightlogger.backend.domain.country.entity.Country;
import com.flightlogger.backend.domain.country.entity.CountryMapper;
import com.flightlogger.backend.model.AirportCreateDto;
import com.flightlogger.backend.model.AirportReadDto;
import com.flightlogger.backend.model.AirportUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = { StringFormatter.class, CountryMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AirportMapper {

    @Mapping(source = "icaoCode", target = "icao")
    @Mapping(source = "iataCode", target = "iata")
    AirportReadDto toDto(Airport airport);

    @Mapping(target = "icaoCode", source = "dto.icao", qualifiedByName = "toUpperCase")
    @Mapping(target = "iataCode", source = "dto.iata", qualifiedByName = "toUpperCase")
    @Mapping(target = "name", source = "dto.name", qualifiedByName = "toUpperCase")
    @Mapping(target = "city", source = "dto.city", qualifiedByName = "toUpperCase")
    @Mapping(target = "timezone", source = "dto.timezone")
    @Mapping(target = "country", source = "countryEntity")
    Airport toEntity(AirportCreateDto dto, Country countryEntity);

    @Mapping(target = "iataCode", source = "dto.iata", qualifiedByName = "toUpperCase")
    @Mapping(target = "name", source = "dto.name", qualifiedByName = "toUpperCase")
    @Mapping(target = "city", source = "dto.city", qualifiedByName = "toUpperCase")
    @Mapping(target = "timezone", source = "dto.timezone")
    @Mapping(target = "country", source = "countryEntity")
    void updateFromDto(AirportUpdateDto dto, Country countryEntity, @MappingTarget Airport airport);
}
