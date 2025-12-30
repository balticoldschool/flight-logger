package com.flightlogger.backend.domain.country.controller;

import com.flightlogger.backend.config.BaseControllerIT;
import com.flightlogger.backend.domain.country.entity.CountryMapper;
import com.flightlogger.backend.domain.country.entity.CountryRepository;
import com.flightlogger.backend.model.CountryReadDto;
import com.flightlogger.backend.model.PagedCountryReadResponse;
import com.flightlogger.backend.model.PaginationMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CountryControllerTest extends BaseControllerIT {

    final static int DEFAULT_PAGE_SIZE = 15;
    final static int DEFAULT_PAGE_NUMBER = 0;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryMapper countryMapper;

    final String BASE_URL = "/countries";

    long dbCountBefore;

    @BeforeEach
    void setUp() {
        dbCountBefore = countryRepository.count();
        assertThat(dbCountBefore).isGreaterThan(DEFAULT_PAGE_SIZE);
    }

    @Nested
    @DisplayName("Get all countries")
    class GetAllCountries {

        @Test
        @DisplayName("Should return a paginated list of countries with default size and page")
        void getAllCountries_NoParameters_Success() throws Exception {
            // when
            MockHttpServletResponse response = performGetRequest(BASE_URL);
            PagedCountryReadResponse responseData = readResponseBody(response, PagedCountryReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            validateResponseContent(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, responseData.getContent());
            validatePaginationMetaDate(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, responseData.getMetadata());
        }

        @Test
        @DisplayName("Should return a paginated list of countries with custom size and page")
        void getAllCountries_CustomParameters_Success() throws Exception {
            // given
            int expectedPageSize = 3;
            int currentPage = 1;

            // when
            MockHttpServletResponse response =
                    performGetRequest(BASE_URL + "?page={p}&size={s}", currentPage, expectedPageSize);
            PagedCountryReadResponse responseData = readResponseBody(response, PagedCountryReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            validateResponseContent(currentPage, expectedPageSize, responseData.getContent());
            validatePaginationMetaDate(currentPage, expectedPageSize, responseData.getMetadata());
        }

        // ToDo: page higher than max - content should be empty

        // Todo: invalid Page size (negative int, decimal or null)

        // ToDo: invalid page number (decimal or null)

        private void validateResponseContent(int currentPage, int expectedPageSize, List<CountryReadDto> responseContent) {
            Pageable pageable = PageRequest.of(currentPage, expectedPageSize);
            List<CountryReadDto> countries = countryRepository.findAll(pageable).getContent().stream().map(countryMapper::toDto).toList();

            assertThat(responseContent).isNotNull();
            assertThat(responseContent).hasSize(expectedPageSize);
            assertThat(responseContent).containsExactlyElementsOf(countries);
        }
    }

    void validatePaginationMetaDate(int currentPage, int expectedPageSize, PaginationMetadata metadata) {
        assertThat(metadata).isNotNull();
        assertThat(metadata.getPageNumber()).isEqualTo(currentPage);
        assertThat(metadata.getPageSize()).isEqualTo(expectedPageSize);
        assertThat(metadata.getTotalElements()).isEqualTo(dbCountBefore);
        assertThat(metadata.getTotalPages()).isEqualTo((int) Math.ceil((double) dbCountBefore / expectedPageSize));
    }
}