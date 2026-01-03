package com.flightlogger.backend.domain.country.service;

import com.flightlogger.backend.domain.country.entity.CountryMapper;
import com.flightlogger.backend.domain.country.entity.CountryRepository;
import com.flightlogger.backend.model.CountryReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Override
    public Page<CountryReadDto> getAllCountries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return countryRepository.findAll(pageable).map(countryMapper::toDto);
    }
}
