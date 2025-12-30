package com.flightlogger.backend.domain.country.controller;

import com.flightlogger.backend.api.CountriesApi;
import com.flightlogger.backend.domain.country.service.CountryService;
import com.flightlogger.backend.model.CountryReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CountryController implements CountriesApi {

    private final CountryService countryService;

    @Override
    public ResponseEntity<List<CountryReadDto>> getAllCountries() {
        return ResponseEntity.ok().body(countryService.getAllCountries());
    }
}
