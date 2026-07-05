package com.flightlogger.backend.domain.flight.entity;

import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.domain.airline.entity.AirlineMapper;
import com.flightlogger.backend.domain.airport.entity.Airport;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.model.FlightCreateDto;
import com.flightlogger.backend.model.FlightReadDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {AirportMapper.class, AirlineMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlightMapper {

    FlightReadDto toDto(Flight flight);

    @Mapping(target = "flightDate", source = "dto.flightDate")
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "destination", source = "destination")
    @Mapping(target = "airline", source = "airline")
    @Mapping(target = "flightNumber", source = "dto.flightNumber")
    @Mapping(target = "bookingClass", source = "dto.bookingClass")
    @Mapping(target = "durationInMinutes", source = "dto.durationInMinutes")
    @Mapping(target = "purpose", source = "dto.purpose")
    Flight toEntity(FlightCreateDto dto, Airport origin, Airport destination, Airline airline);
}
