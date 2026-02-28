package com.flightlogger.backend.domain.airport.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flightlogger.backend.api.AirportsApi;
import com.flightlogger.backend.config.BaseControllerIT;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.domain.airport.entity.AirportRepository;
import com.flightlogger.backend.model.AirportReadDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.stream.Stream;

import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT;
import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT_READ_DTO;
import static com.flightlogger.backend.testdata.ErrorMessages.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AirportControllerIT extends BaseControllerIT {

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private AirportMapper airportMapper;

    private Long dbCountBefore;

    @BeforeEach
    void setUp() {
        if (dbCountBefore == null) {
            dbCountBefore = airportRepository.count();
        }
    }

    @Nested
    @DisplayName("getAllAirports")
    class GetAllAirports {

        @Test
        @DisplayName("Should return all available airports")
        void getAllAirports_Success() throws Exception {
            // given
            final List<AirportReadDto> expectedAirports =
                    airportRepository.findAll().stream().map(airportMapper::toDto).toList();
            assertThat(dbCountBefore).isGreaterThan(0);

            // when
            final MockHttpServletResponse response = performGetRequest(AirportsApi.PATH_GET_ALL_AIRPORTS);
            final List<AirportReadDto> airports = readResponseBody(response, new TypeReference<>(){});

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(airports).hasSize(dbCountBefore.intValue());
            assertThat(airports).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedAirports);
        }
    }

    @Nested
    @DisplayName("getAirportByIcao")
    class GetAirportByIcao {

        @Test
        @DisplayName("Should return desired airport")
        void getAirportByIcao_Success() throws Exception {
            // given
            final String icaoCode = FRANKFURT_AIRPORT.getIcaoCode();

            // when
            MockHttpServletResponse response = performGetRequest(AirportsApi.PATH_GET_AIRPORT_BY_ICAO, icaoCode.toLowerCase());
            AirportReadDto airport = readResponseBody(response, AirportReadDto.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(airport).isEqualTo(FRANKFURT_AIRPORT_READ_DTO);
        }

        @Test
        @DisplayName("Should return 404 with corrseponding message when no airoprt was found")
        void getAirportByIcao_AirportNotFound_ReturnNotFoundException() throws Exception {
            // given
            final String invalidIcaoCode = "fooo";

            // when & then
            performAndValidateException(
                    performGetRequest(AirportsApi.PATH_GET_AIRPORT_BY_ICAO, invalidIcaoCode),
                    HttpStatus.NOT_FOUND,
                    NOT_FOUND_ERROR_TITLE,
                    String.format(AIRPORT_NOT_FOUND, invalidIcaoCode.toUpperCase()),
                    dbCountBefore,
                    airportRepository::count
            );
        }

        @Nested
        @DisplayName("Parameter validation")
        class ParameterValidation {

            ///  A list of invalid icao codes
            private static Stream<Arguments> invalidIcaoCodes() {
                return Stream.of(
                        Arguments.of("ICAO is too long", "foooo"),
                        Arguments.of("ICAO is too short", "foo"),
                        Arguments.of("ICAO contains invalid characters", "foo!"),
                        Arguments.of("ICAO contains numbers", "ED12"),
                        Arguments.of("ICAO contains spaces", "ED F"),
                        Arguments.of("ICAO is just spaces", "    ")
                );
            }

            @ParameterizedTest(name = "Should return 400 Bad Request with corresponding error message, when {0}")
            @MethodSource("invalidIcaoCodes")
            void getAirportByIcao_InvalidPathParameter_ReturnBadRequest(Object ignores, String icaoCode) throws Exception {
                // when & then
                performAndValidateException(
                        performGetRequest(AirportsApi.PATH_GET_AIRPORT_BY_ICAO, icaoCode),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        INVALID_ICAO_CODE_MESSAGE,
                        dbCountBefore,
                        airportRepository::count
                );
            }
        }
    }
}
