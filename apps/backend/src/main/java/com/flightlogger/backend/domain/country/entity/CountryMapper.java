package com.flightlogger.backend.domain.country.entity;

import com.flightlogger.backend.common.utils.StringFormatter;
import com.flightlogger.backend.model.CountryCreateDto;
import com.flightlogger.backend.model.CountryReadDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = StringFormatter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper {

    CountryReadDto toDto(Country country);

    @Mapping(target = "isoCode2", qualifiedByName = "toUpperCase")
    @Mapping(target = "isoCode3", qualifiedByName = "toUpperCase")
    Country toEntity(CountryCreateDto dto);
}
