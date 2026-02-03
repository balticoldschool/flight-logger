package com.flightlogger.backend.domain.country.service;

import com.flightlogger.backend.domain.country.entity.Country;
import com.flightlogger.backend.domain.country.entity.CountryMapper;
import com.flightlogger.backend.domain.country.entity.CountryRepository;
import com.flightlogger.backend.domain.country.exception.CountryAlreadyExistsException;
import com.flightlogger.backend.domain.country.exception.CountryConflictException;
import com.flightlogger.backend.domain.country.exception.CountryNotFoundException;
import com.flightlogger.backend.model.CountryCreateDto;
import com.flightlogger.backend.model.CountryReadDto;
import com.flightlogger.backend.model.CountryUpdateDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    @Transactional
    public CountryReadDto saveCountry(CountryCreateDto dto) {
        String iso2Code = StringUtils.upperCase(dto.getIsoCode2());
        String iso3Code = StringUtils.upperCase(dto.getIsoCode3());

        if (countryRepository.existsByIsoCode2(iso2Code)) {
            throw new CountryAlreadyExistsException(iso2Code);
        }

        if (countryRepository.existsByIsoCode3(iso3Code)) {
            throw new CountryAlreadyExistsException(iso3Code);
        }

        Country savedCountry = countryRepository.save(countryMapper.toEntity(dto));

        return countryMapper.toDto(savedCountry);
    }

    @Override
    @Transactional
    public CountryReadDto updateCountryById(UUID id, CountryUpdateDto dto) {
        String iso2Code = StringUtils.upperCase(dto.getIsoCode2());
        String iso3Code = StringUtils.upperCase(dto.getIsoCode3());

        Country country = countryRepository.findById(id).orElseThrow(() -> new CountryNotFoundException(id));

        if (!iso2Code.equals(country.getIsoCode2()) && countryRepository.existsByIsoCode2(iso2Code)) {
            throw new CountryAlreadyExistsException(iso2Code);
        }

        if (!iso3Code.equals(country.getIsoCode3()) && countryRepository.existsByIsoCode3(iso3Code)) {
            throw new CountryAlreadyExistsException(iso3Code);
        }

        countryMapper.updateFromDto(dto, country);

        return countryMapper.toDto(country);
    }
}
