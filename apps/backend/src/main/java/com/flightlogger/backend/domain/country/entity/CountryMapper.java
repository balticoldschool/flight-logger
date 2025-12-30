package com.flightlogger.backend.domain.country.entity;

import com.flightlogger.backend.model.CountryReadDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper {

    CountryReadDto toDto(Country country);
}
