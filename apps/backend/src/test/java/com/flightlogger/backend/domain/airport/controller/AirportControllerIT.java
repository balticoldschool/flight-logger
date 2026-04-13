package com.flightlogger.backend.domain.airport.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flightlogger.backend.api.AirportsApi;
import com.flightlogger.backend.config.BaseControllerIT;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.domain.airport.entity.AirportRepository;
import com.flightlogger.backend.model.AirportCreateDto;
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
import utils.GenericObjectMutator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT;
import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT_READ_DTO;
import static com.flightlogger.backend.testdata.CountryTestData.GERMANY_COUNTRY;
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
                    String.format(AIRPORT_NOT_FOUND_MESSAGE, invalidIcaoCode.toUpperCase()),
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

    @Nested
    @DisplayName("createAirport")
    class CreateAirport {
        AirportCreateDto newAirport;

        @BeforeEach
        void setUp() {
            newAirport =  new AirportCreateDto()
                    .icao("etnl")
                    .iata("rlg")
                    .name("Rotock Laage")
                    .city("Rostock")
                    .countryId(GERMANY_COUNTRY.getId())
                    .timezone("Europe/Berlin");
        }

        @Test
        @DisplayName("Should create new Airport and return 201")
        void createAirport_Success() throws Exception {
            // given
            assertThat(airportRepository.existsById(newAirport.getIcao().toUpperCase())).isFalse();

            // when
            MockHttpServletResponse response = performPostRequest(AirportsApi.PATH_CREATE_AIRPORT, newAirport);
            AirportReadDto createdAirport = readResponseBody(response, AirportReadDto.class);
            long dbCountAfter = airportRepository.count();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(dbCountAfter).isEqualTo(dbCountBefore + 1);
            assertThat(airportRepository.existsById(newAirport.getIcao().toUpperCase())).isTrue();

            assertThat(createdAirport.getIcao()).isEqualTo(newAirport.getIcao().toUpperCase());
            assertThat(createdAirport.getIata()).isEqualTo(newAirport.getIata().toUpperCase());
            assertThat(createdAirport.getName()).isEqualTo(newAirport.getName().toUpperCase());
            assertThat(createdAirport.getCity()).isEqualTo(newAirport.getCity().toUpperCase());
            assertThat(createdAirport.getCountry().getId()).isEqualTo(newAirport.getCountryId());
            assertThat(createdAirport.getTimezone()).isEqualTo(newAirport.getTimezone());
        }

        @Nested
        @DisplayName("Conflicts - should not create airport and return 409 Conflict -")
        class Conflicts {

            /// A list of airport codes that already exist in the database
            /// Argument Format: duplicated_field, duplicated_value, mutator
            private static Stream<Arguments> conflictingAirportCodes() {
                return Stream.of(
                        Arguments.of("ICAO", FRANKFURT_AIRPORT.getIcaoCode(), (GenericObjectMutator<AirportCreateDto>) dto -> dto.setIcao(FRANKFURT_AIRPORT.getIcaoCode())),
                        Arguments.of("IATA", FRANKFURT_AIRPORT.getIataCode(), (GenericObjectMutator<AirportCreateDto>) dto -> dto.setIata(FRANKFURT_AIRPORT.getIataCode()))
                );
            }

            @ParameterizedTest(name = "Should not create airport and return 409 conflict when {0} already exists in DB")
            @MethodSource("conflictingAirportCodes")
            void createAirport_CodeAlreadyExists_ReturnConflict(String code, String updatedValue, GenericObjectMutator<AirportCreateDto> mutator) throws Exception {
                // given
                mutator.accept(newAirport);

                // when & then
                performAndValidateException(
                        performPostRequest(AirportsApi.PATH_CREATE_AIRPORT, newAirport),
                        HttpStatus.CONFLICT,
                        CONFLICT_ERROR_TITLE,
                        String.format(AIRPORT_ALREADY_EXISTS, code, updatedValue),
                        dbCountBefore,
                        airportRepository::count
                );
            }
        }

        @Nested
        @DisplayName("DTO validation")
        class DtoValidation {

            private Map<String, Object> payload;

            @BeforeEach
            void setUp() {
                payload = new HashMap<>(Map.of(
                        "icao", "ETNL",
                        "iata", "RLG",
                        "name", "Rostock Laage",
                        "city", "Rostock",
                        "countryId", GERMANY_COUNTRY.getId().toString(),
                        "timezone", "Europe/Berlin"
                ));
            }

            /// A list of test scenarios with invalid AirportCreateDto
            /// Argument Format: description, mutator, expected_error_message
            private static Stream<Arguments> invalidDtos() {
                return Stream.of(
                        Arguments.of("ICAO is null", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setIcao(null), MANDATORY_ICAO_MISSING_MESSAGE),
                        Arguments.of("ICAO is missing", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setIcao(null), MANDATORY_ICAO_MISSING_MESSAGE),
                        Arguments.of("ICAO is invalid", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setIcao("1abc"), INVALID_ICAO_CODE_MESSAGE),
                        Arguments.of("ICAO is invalid", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setIcao("AB"), INVALID_ICAO_CODE_MESSAGE),
                        Arguments.of("IATA is null", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setIata(null), MANDATORY_IATA_MISSING_MESSAGE),
                        Arguments.of("IATA is invalid", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setIata("abcde"), INVALID_IATA_CODE_MESSAGE),
                        Arguments.of("Name is null", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setName(null), MANDATORY_NAME_MISSING_MESSAGE),
                        Arguments.of("Name is blank", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setName(""), INVALID_NAME_MESSAGE),
                        Arguments.of("City is null", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setCity(null), MANDATORY_CITY_MISSING_MESSAGE),
                        Arguments.of("City is blank", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setCity(""), INVALID_CITY_CODE_MESSAGE),
                        Arguments.of("CountryId is null", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setCountryId(null), MANDATORY_COUNTRYID_MISSING_MESSAGE),
                        Arguments.of("Timezone is null", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setTimezone(null), MANDATORY_TIMEZONE_MISSING_MESSAGE),
                        Arguments.of("Timezone is blank", (GenericObjectMutator<AirportCreateDto>) dto -> dto.setTimezone("foo bar"), String.format(INVALID_TIMEZONE_MESSAGE, "foo bar"))
                );
            }

            /// A list of test scenarios with missing fields in the request payload
            /// Argument Format: field_name, field_key, expected_error_message
            private static Stream<Arguments> missingFields() {
                return Stream.of(
                        Arguments.of("ICAO", "icao", MANDATORY_ICAO_MISSING_MESSAGE),
                        Arguments.of("IATA", "iata", MANDATORY_IATA_MISSING_MESSAGE),
                        Arguments.of("name", "name", MANDATORY_NAME_MISSING_MESSAGE),
                        Arguments.of("city", "city", MANDATORY_CITY_MISSING_MESSAGE),
                        Arguments.of("countryId", "countryId", MANDATORY_COUNTRYID_MISSING_MESSAGE),
                        Arguments.of("timezone", "timezone", MANDATORY_TIMEZONE_MISSING_MESSAGE)
                );
            }

            @ParameterizedTest(name = "Should return 400 Bad Request when {0}")
            @MethodSource("invalidDtos")
            void createAirport_InvalidDto_ReturnBadRequest(Object ignored, GenericObjectMutator<AirportCreateDto> mutator, String expectedMessage) throws Exception {
                // given
                mutator.accept(newAirport);

                // when & then
                performAndValidateDtoValidation(newAirport, expectedMessage);
            }

            @ParameterizedTest(name = "Should return 400 Bad Request when {0} is missing")
            @MethodSource("missingFields")
            void createAirport_MissingField_ReturnBadRequest(Object ignored, String fieldKey, String expectedMessage) throws Exception {
                // given
                payload.remove(fieldKey);

                // when & then
                performAndValidateDtoValidation(payload, expectedMessage);
            }

            private void performAndValidateDtoValidation(Object payload, String expectedDetail) throws Exception {
                performAndValidateException(
                        performPostRequest(AirportsApi.PATH_CREATE_AIRPORT, payload),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        expectedDetail,
                        dbCountBefore,
                        airportRepository::count
                );
            }
        }
    }
}
