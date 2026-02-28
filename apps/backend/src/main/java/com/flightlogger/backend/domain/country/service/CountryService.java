package com.flightlogger.backend.domain.country.service;

import com.flightlogger.backend.model.CountryCreateDto;
import com.flightlogger.backend.model.CountryReadDto;
import com.flightlogger.backend.model.CountryUpdateDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface CountryService {
    Page<CountryReadDto> getAllCountries(String search, int page, int size);

    void deleteCountryById(UUID id);

    CountryReadDto saveCountry(CountryCreateDto dto);

    CountryReadDto updateCountryById(UUID id, CountryUpdateDto dto);

    CountryReadDto getCountryById(UUID id);
}
