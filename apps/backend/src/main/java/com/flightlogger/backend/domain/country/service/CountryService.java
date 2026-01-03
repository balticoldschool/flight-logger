package com.flightlogger.backend.domain.country.service;

import com.flightlogger.backend.model.CountryReadDto;
import org.springframework.data.domain.Page;

public interface CountryService {
    Page<CountryReadDto> getAllCountries(int page, int size);
}
