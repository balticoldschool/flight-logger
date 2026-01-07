package com.flightlogger.backend.domain.country.controller;

import com.flightlogger.backend.api.CountriesApi;
import com.flightlogger.backend.common.utils.PaginationUtils;
import com.flightlogger.backend.domain.country.service.CountryService;
import com.flightlogger.backend.model.CountryReadDto;
import com.flightlogger.backend.model.PagedCountryReadResponse;
import com.flightlogger.backend.model.PaginationMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CountryController implements CountriesApi {

    private final CountryService countryService;

    @Override
    public ResponseEntity<PagedCountryReadResponse> getAllCountries(String search, Integer page, Integer pageSize) {
        Page<CountryReadDto> pagedCountryRead = countryService.getAllCountries(search, page, pageSize);
        PaginationMetadata metadata = PaginationUtils.toMetaData(pagedCountryRead);

        return ResponseEntity.ok(new PagedCountryReadResponse(pagedCountryRead.getContent(), metadata));
    }
}
