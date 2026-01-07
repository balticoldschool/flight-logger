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
            Page<CountryReadDto> result = countryService.getAllCountries(null, currentPage, pageSize);

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
            Page<CountryReadDto> result = countryService.getAllCountries(null, currentPage, pageSize);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            validatePageMetadata(result, pageSize, currentPage);
        }

        @Test
        @DisplayName("Should return exactly canada when searching '🇨🇦'")
        void getAllCountries_SearchWithUnambiguousPhrase_Success() {
            // given
            String searchQuery = "🇨🇦";
            int currentPage = 0;
            int pageSize = 8;

            // when
            Page<CountryReadDto> result = countryService.getAllCountries(searchQuery, currentPage, pageSize);

            // then
            assertThat(result.getContent())
                    .hasSize(1)
                    .extracting(CountryReadDto::getName)
                    .containsExactly("Canada");
        }

        @Test
        @DisplayName("Should return multiple matches and respect relevance (ISO first)")
        void getAllCountries_SearchWithAmbiguousPhrase_Success() {
            // given
            String searchQuery = "DE";

            // when
            Page<CountryReadDto> result = countryService.getAllCountries(searchQuery, 0, 10);

            // then
            assertThat(result.getContent()).hasSizeGreaterThan(1);
            assertThat(result.getContent().getFirst().getIsoCode2()).isEqualTo("DE");
            assertThat(result.getContent().getFirst().getName()).contains("Germany");
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