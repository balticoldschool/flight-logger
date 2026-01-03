package com.flightlogger.backend.domain.country.service;

import com.flightlogger.backend.annotations.IntegrationTest;
import com.flightlogger.backend.domain.country.entity.CountryRepository;
import com.flightlogger.backend.model.CountryReadDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class CountryServiceImplIT {

    @Autowired
    private CountryService countryService;

    @Autowired
    private CountryRepository countryRepository;

    long dbCountBefore;

    @BeforeEach
    void setUp() {
        dbCountBefore = countryRepository.count();
    }

    @Nested
    @DisplayName("GetAllCountries")
    class GetAllCountries {

        @Test
        @DisplayName("Should return paginated list of CountryReadDto")
        public void getAllCountries_Success() {
            // given
            int currentPage = 0;
            int pageSize = 8;

            // when
            Page<CountryReadDto> result = countryService.getAllCountries(currentPage, pageSize);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize((int) Math.min(pageSize, dbCountBefore));
            validatePageMetadata(result, pageSize, currentPage);
        }

        @Test
        @DisplayName("Should return empty content when page number greater than max page number")
        void getAllCountries_InvalidPageNumber_Success() {
            // given
            int currentPage = 100;
            int pageSize = 8;

            // when
            Page<CountryReadDto> result = countryService.getAllCountries(currentPage, pageSize);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            validatePageMetadata(result, pageSize, currentPage);
        }

        private void validatePageMetadata(Page<?> page, int pageSize, int currentPage) {
            int expectedPageCount = (int) Math.ceil((double) dbCountBefore / pageSize);

            assertThat(page.getTotalElements()).isEqualTo(dbCountBefore);
            assertThat(page.getTotalPages()).isEqualTo(expectedPageCount);
            assertThat(page.getNumber()).isEqualTo(currentPage);
            assertThat(page.getSize()).isEqualTo(pageSize);
        }
    }
}