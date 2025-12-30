package com.flightlogger.backend.domain.country.service;

import com.flightlogger.backend.model.CountryReadDto;

import java.util.List;

public interface CountryService {
    List<CountryReadDto> getAllCountries();
}
