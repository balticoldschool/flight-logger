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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.stream.Stream;

import static com.flightlogger.backend.testdata.ErrorMessages.*;
import static org.assertj.core.api.Assertions.assertThat;

class CountryControllerTest extends BaseControllerIT {

    final static int DEFAULT_PAGE_SIZE = 15;
    final static int DEFAULT_PAGE_NUMBER = 0;
    final static  String BASE_URL = "/countries";
    final static String BASE_URL_WITH_PAGINATION_PARAMS = BASE_URL + "?page={p}&pageSize={s}";

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryMapper countryMapper;

    long dbCountBefore;

    @BeforeEach
    void setUp() {
        dbCountBefore = countryRepository.count();
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
            validatePaginationMetaData(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, responseData.getMetadata());
        }

        @Test
        @DisplayName("Should return a paginated list of countries with custom size and page")
        void getAllCountries_CustomParameters_Success() throws Exception {
            // given
            int expectedPageSize = 3;
            int currentPage = 1;

            // when
            MockHttpServletResponse response =
                    performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, currentPage, expectedPageSize);
            PagedCountryReadResponse responseData = readResponseBody(response, PagedCountryReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            validateResponseContent(currentPage, expectedPageSize, responseData.getContent());
            validatePaginationMetaData(currentPage, expectedPageSize, responseData.getMetadata());
        }

        @Test
        @DisplayName("Should return empty content when current index is higher than maximum")
        void getAllCountries_InvalidPageNumber_Success() throws Exception {
            // given
            int currentPage = 5;
            int expectedPageSize = (int) dbCountBefore;

            // when
            MockHttpServletResponse response =
                    performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, currentPage, expectedPageSize);
            PagedCountryReadResponse responseData = readResponseBody(response, PagedCountryReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            validatePaginationMetaData(currentPage, expectedPageSize, responseData.getMetadata());
            assertThat(responseData.getContent()).isEmpty();
        }

        @Nested
        class InvalidPageSize {

            /// Set of invalid page size arguments
            private static Stream<Arguments> invalidPageSizes() {
                return Stream.of(
                        Arguments.of(0),
                        Arguments.of(-1),
                        Arguments.of("foo bar"),
                        Arguments.of(1.5)
                );
            }

            @ParameterizedTest(name = "Invalid page size {0} should return 400 Bad Request")
            @MethodSource("invalidPageSizes")
            void getAllCountries_InvalidPageSize_ReturnsBadRequest(Object pageSize) throws Exception {
                // given
                int currentPage = 0;

                // when & then
                performAndValidateException(performGetRequest(
                                BASE_URL_WITH_PAGINATION_PARAMS, currentPage, pageSize),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        INVALID_PAGE_SIZE_MESSAGE,
                        dbCountBefore,
                        () -> countryRepository.count()
                );
            }
        }

        @Nested
        class InvalidPageNumber {

            /// Set of invalid page arguments
            private static Stream<Arguments> invalidPageNumbers() {
                return Stream.of(
                        Arguments.of(-1),
                        Arguments.of(1.5),
                        Arguments.of("foo bar")
                );
            }

            @ParameterizedTest(name = "Invalid current page {0} shoult return 400 Bad Request")
            @MethodSource("invalidPageNumbers")
            void getAllCountries_InvalidPage_ReturnsBadRequest(Object pageNumber) throws Exception {
                // given
                int expectedPageSize = 15;

                // when & then
                performAndValidateException(
                        performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, pageNumber, expectedPageSize),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        INVALID_PAGE_NUMBER_MESSAGE,
                        dbCountBefore,
                        () -> countryRepository.count());
            }
        }

        /**
         * Compares the API response against a fresh database query to ensure data integrity.
         *
         * @param currentPage Zero-based page index.
         * @param expectedPageSize Number of elements requested.
         * @param responseContent DTOs returned by the controller.
         */
        private void validateResponseContent(int currentPage, int expectedPageSize, List<CountryReadDto> responseContent) {
            Pageable pageable = PageRequest.of(currentPage, expectedPageSize);
            List<CountryReadDto> countries = countryRepository.findAll(pageable).getContent().stream().map(countryMapper::toDto).toList();

            assertThat(responseContent).isNotNull();
            assertThat(responseContent).hasSize(expectedPageSize);
            assertThat(responseContent).containsExactlyElementsOf(countries);
        }
    }

    /**
     * Verifies pagination metadata against the request parameters and database state.
     *
     * @param currentPage Expected zero-based page index.
     * @param expectedPageSize Expected number of elements per page.
     * @param metadata The pagination details returned by the API.
     */
    void validatePaginationMetaData(int currentPage, int expectedPageSize, PaginationMetadata metadata) {
        assertThat(metadata).isNotNull();
        assertThat(metadata.getPageNumber()).isEqualTo(currentPage);
        assertThat(metadata.getPageSize()).isEqualTo(expectedPageSize);
        assertThat(metadata.getTotalElements()).isEqualTo(dbCountBefore);
        assertThat(metadata.getTotalPages()).isEqualTo((int) Math.ceil((double) dbCountBefore / expectedPageSize));
    }
}