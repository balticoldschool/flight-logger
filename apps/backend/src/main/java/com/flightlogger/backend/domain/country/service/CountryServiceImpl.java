package com.flightlogger.backend.domain.country.service;

import com.flightlogger.backend.domain.country.entity.CountryMapper;
import com.flightlogger.backend.domain.country.entity.CountryRepository;
import com.flightlogger.backend.domain.country.exception.CountryConflictException;
import com.flightlogger.backend.domain.country.exception.CountryNotFoundException;
import com.flightlogger.backend.model.CountryReadDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Override
    public Page<CountryReadDto> getAllCountries(String search, int page, int size) {
        String sanitizedSearch = search == null ? "" : search.strip();

        if (sanitizedSearch.isBlank()) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            return countryRepository.findAll(pageable).map(countryMapper::toDto);
        }

        Pageable pageable = PageRequest.of(page, size);
        return countryRepository.searchWithString(sanitizedSearch, pageable).map(countryMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteCountryById(UUID id) {
        // check if a country exists
        if (!countryRepository.existsById(id)) {
            throw new CountryNotFoundException(id);
        }

        // delete country or throw exception if the country is still used
        try {
            countryRepository.deleteById(id);
            countryRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new CountryConflictException(id);
        }
    }
}
