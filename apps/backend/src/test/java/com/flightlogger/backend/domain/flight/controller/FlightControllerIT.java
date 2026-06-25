package com.flightlogger.backend.domain.flight.controller;

import com.flightlogger.backend.api.FlightsApi;
import com.flightlogger.backend.config.BaseControllerIT;
import com.flightlogger.backend.domain.flight.entity.FlightMapper;
import com.flightlogger.backend.domain.flight.entity.FlightRepository;
import com.flightlogger.backend.model.FlightReadDto;
import com.flightlogger.backend.model.PagedFlightReadResponse;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.flightlogger.backend.testdata.ErrorMessages.*;
import static com.flightlogger.backend.testdata.FlightTestData.FRA_MUC_FLIGHT_READ_DTO;
import static org.assertj.core.api.Assertions.assertThat;

// Loads the flight data - this is not part of liquibase to avoid FK constraint violations
@Sql(scripts = "classpath:db/test/flight_test_data.sql")
class FlightControllerIT extends BaseControllerIT {

    static final int DEFAULT_PAGE_SIZE = 15;
    static final int DEFAULT_PAGE_NUMBER = 0;
    static final String BASE_URL_WITH_PAGINATION_PARAMS = FlightsApi.PATH_GET_ALL_FLIGHTS + "?page={p}&pageSize={s}";

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightMapper flightMapper;

    private Long dbCountBefore;

    @BeforeEach
    void setUp() {
        if (dbCountBefore == null) dbCountBefore = flightRepository.count();
    }

    @Nested
    @DisplayName("Get all flights")
    class GetAllFlights {

        @Test
        @DisplayName("Should return a paginated list of flights with default size and page")
        void getAllFlights_NoParameters_Success() throws Exception {
            // when
            MockHttpServletResponse response = performGetRequest(FlightsApi.PATH_GET_ALL_FLIGHTS);
            PagedFlightReadResponse responseData = readResponseBody(response, PagedFlightReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            validateResponseContent(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, responseData.getContent());
            validatePaginationMetaData(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, responseData.getMetadata(), dbCountBefore);
        }

        @Test
        @DisplayName("Should return a paginated list of flights with custom size and page")
        void getAllFlights_CustomParameters_Success() throws Exception {
            // given
            int currentPage = 1;
            int pageSize = 1;

            // when
            MockHttpServletResponse response = performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, currentPage, pageSize);
            PagedFlightReadResponse responseData = readResponseBody(response, PagedFlightReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            validateResponseContent(currentPage, pageSize, responseData.getContent());
            validatePaginationMetaData(currentPage, pageSize, responseData.getMetadata(), dbCountBefore);
        }

        @Test
        @DisplayName("Should return empty content when page index is higher than maximum")
        void getAllFlights_OutOfBoundsPage_ReturnsEmptyContent() throws Exception {
            // given
            int currentPage = 5;
            int pageSize = dbCountBefore.intValue();

            // when
            MockHttpServletResponse response = performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, currentPage, pageSize);
            PagedFlightReadResponse responseData = readResponseBody(response, PagedFlightReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            validatePaginationMetaData(currentPage, pageSize, responseData.getMetadata(), dbCountBefore);
            assertThat(responseData.getContent()).isEmpty();
        }

        @Nested
        class InvalidPageSize {

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
            void getAllFlights_InvalidPageSize_ReturnsBadRequest(Object pageSize) throws Exception {
                // given
                int currentPage = 0;

                // when & then
                performAndValidateException(
                        performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, currentPage, pageSize),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        INVALID_PAGE_SIZE_MESSAGE,
                        dbCountBefore,
                        () -> flightRepository.count()
                );
            }
        }

        @Nested
        class InvalidPageNumber {

            private static Stream<Arguments> invalidPageNumbers() {
                return Stream.of(
                        Arguments.of(-1),
                        Arguments.of(1.5),
                        Arguments.of("foo bar")
                );
            }

            @ParameterizedTest(name = "Invalid page number {0} should return 400 Bad Request")
            @MethodSource("invalidPageNumbers")
            void getAllFlights_InvalidPageNumber_ReturnsBadRequest(Object pageNumber) throws Exception {
                // given
                int pageSize = 15;

                // when & then
                performAndValidateException(
                        performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, pageNumber, pageSize),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        INVALID_PAGE_NUMBER_MESSAGE,
                        dbCountBefore,
                        () -> flightRepository.count()
                );
            }
        }

        /**
         * Compares the API response against a fresh database query to ensure data integrity.
         *
         * @param currentPage Zero-based page index.
         * @param pageSize    Number of elements requested.
         * @param responseContent DTOs returned by the controller.
         */
        private void validateResponseContent(int currentPage, int pageSize, List<FlightReadDto> responseContent) {
            Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by("flightDate").ascending());
            List<FlightReadDto> expected = flightRepository.findAll(pageable).getContent().stream().map(flightMapper::toDto).toList();

            assertThat(responseContent).isNotNull();
            assertThat(responseContent).hasSize(expected.size());
            assertThat(responseContent).containsExactlyElementsOf(expected);
        }
    }

    @Nested
    @DisplayName("Get flight by id")
    class GetFlightById {

        @Test
        @DisplayName("Should return desired Flight")
        void getFlightById_Success() throws Exception {
            // given
            final FlightReadDto expectedFlight = FRA_MUC_FLIGHT_READ_DTO;

            // when
            MockHttpServletResponse response = performGetRequest(
                    FlightsApi.PATH_GET_FLIGHT_BY_ID, expectedFlight.getId()
            );
            FlightReadDto flight = readResponseBody(response, FlightReadDto.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(flight).isEqualTo(expectedFlight);
        }

        // not found
        @Test
        @DisplayName("Should return 409 with corresponding message when no flight was found")
        void getFlightById_FlightNotFound_ReturnNotFoundException() throws Exception {
            // given
            final UUID invalidId = new UUID(0L, 0L);

            // when & then
            performAndValidateException(
                    performGetRequest(FlightsApi.PATH_GET_FLIGHT_BY_ID, invalidId),
                    HttpStatus.NOT_FOUND,
                    NOT_FOUND_ERROR_TITLE,
                    FLIGHTS_NOT_FOUND_MESSAGE,
                    dbCountBefore,
                    flightRepository::count
            );
        }

        // invalid id
        @Nested
        @DisplayName("Parameter Validation")
        class ParameterValidation {

            /// A list of invalid IDs
            private static Stream<Arguments> invalidFlightIds() {
                return Stream.of(
                        Arguments.of("ID is invalid", "foo"),
                        Arguments.of("ID is invalid", "123-abc-!!!-???"),
                        Arguments.of("ID includes illegal character", "z85e30a9-8083-46a4-b743-bb74bf787c9c")
                );
            }

            @ParameterizedTest(name = "Should return 400 bad request with corresponding errro message when {0}")
            @MethodSource("invalidFlightIds")
            void getFlightById_InvalidId_ReturnBadRequest(Object ignored, String id) throws Exception {
                // when & then
                performAndValidateException(
                        performGetRequest(FlightsApi.PATH_GET_FLIGHT_BY_ID, id),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        INVALID_ID_MESSAGE,
                        dbCountBefore,
                        flightRepository::count
                );
            }
        }
    }
}