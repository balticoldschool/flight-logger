package com.flightlogger.backend.domain.airport.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flightlogger.backend.api.AirportsApi;
import com.flightlogger.backend.config.BaseControllerIT;
import com.flightlogger.backend.domain.airport.entity.Airport;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.domain.airport.entity.AirportRepository;
import com.flightlogger.backend.model.AirportCreateDto;
import com.flightlogger.backend.model.AirportReadDto;
import com.flightlogger.backend.model.AirportUpdateDto;
import org.junit.jupiter.api.*;
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
import java.util.UUID;
import java.util.stream.Stream;

import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT;
import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT_READ_DTO;
import static com.flightlogger.backend.testdata.CountryTestData.CANADA_COUNTRY;
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

    @Nested
    @DisplayName("deleteAirportByIcao")
    class DeleteAirportByIcao {

        @Test
        @DisplayName("Should remove desired airport from database and return 204 no content")
        void deleteAirportByIcao_Success() throws Exception {
            // given
            assertThat(airportRepository.existsById(FRANKFURT_AIRPORT.getIcaoCode())).isTrue();

            // when
            MockHttpServletResponse response = performDeleteRequest(
                    AirportsApi.PATH_DELETE_AIRPORT_BY_ICAO, FRANKFURT_AIRPORT.getIcaoCode().toLowerCase()
            );

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(response.getContentAsString()).isEmpty();
            assertThat(airportRepository.existsById(FRANKFURT_AIRPORT.getIcaoCode())).isFalse();
            assertThat(airportRepository.count()).isEqualTo(dbCountBefore - 1);
        }

        @Test
        @DisplayName("Should return 204 no content when airport ICAO code not exits")
        void deleteAirportByIcao_AirportCodeDoesNotExits_NoContent() throws Exception {
            // given
            final String invalidIcaoCode = "fooo";
            assertThat(airportRepository.existsById(invalidIcaoCode.toUpperCase())).isFalse();

            // when
            MockHttpServletResponse response = performDeleteRequest(
                    AirportsApi.PATH_DELETE_AIRPORT_BY_ICAO, invalidIcaoCode.toLowerCase()
            );

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(response.getContentAsString()).isEmpty();
            assertThat(airportRepository.count()).isEqualTo(dbCountBefore);
        }

        @Nested
        class InvalidPathParameters {

            /// A list of invalid airport IDs
            private static Stream<Arguments> invalidAirportIds() {
                return Stream.of(
                        Arguments.of("ICAO is too long", "foooo"),
                        Arguments.of("ICAO is too short", "foo"),
                        Arguments.of("ICAO contains invalid characters", "foo!"),
                        Arguments.of("ICAO contains numbers", "ED12"),
                        Arguments.of("ICAO contains spaces", "ED F")
                );
            }

            @ParameterizedTest(name = "Should return 400 bad request when {0}")
            @MethodSource("invalidAirportIds")
            void deleteAirportByIcao_InvalidIcao_ReturnBadRequest(Object ignored, String icao) throws Exception {
                // when & then
                performAndValidateException(
                        performDeleteRequest(AirportsApi.PATH_DELETE_AIRPORT_BY_ICAO, icao),
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
    @DisplayName("updateAirport")
    class UpdateAirport {
        AirportUpdateDto updateDto;
        final String referencedAirportIcao = FRANKFURT_AIRPORT.getIcaoCode();
        final Airport referenceAirportBeforeUpdate = airportRepository.findById(referencedAirportIcao).orElse(null);

        @BeforeEach
        void setUp() {
            assertThat(referenceAirportBeforeUpdate).isNotNull();
            updateDto = new AirportUpdateDto()
                    .iata("abc")
                    .name("New name Frankfurt")
                    .city("new city")
                    .countryId(CANADA_COUNTRY.getId())
                    .timezone("America/Toronto");
        }

        @Test
        @DisplayName("Should update airport and return http status OK")
        void updateAirport_Success() throws Exception {
            // given
            Assertions.assertNotNull(referenceAirportBeforeUpdate);
            assertThat(referenceAirportBeforeUpdate.getIataCode()).isNotEqualTo(updateDto.getIata().toUpperCase());
            assertThat(referenceAirportBeforeUpdate.getName()).isNotEqualTo(updateDto.getName().toUpperCase());
            assertThat(referenceAirportBeforeUpdate.getCity()).isNotEqualTo(updateDto.getCity().toUpperCase());
            assertThat(referenceAirportBeforeUpdate.getCountry().getId()).isNotEqualTo(updateDto.getCountryId());
            assertThat(referenceAirportBeforeUpdate.getTimezone()).isNotEqualTo(updateDto.getTimezone());

            // when
            MockHttpServletResponse response =
                    performPutRequest(AirportsApi.PATH_UPDATE_AIRPORT_BY_ICAO, updateDto, referencedAirportIcao.toLowerCase());
            AirportReadDto responseBody = readResponseBody(response, AirportReadDto.class);
            Airport airportAfterUpdate = airportRepository.findById(referencedAirportIcao).orElse(null);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(airportRepository.count()).isEqualTo(dbCountBefore);
            assertThat(airportAfterUpdate).isNotNull();

            // assert response body
            assertThat(responseBody.getIcao()).isEqualTo(referencedAirportIcao);
            assertThat(responseBody.getIata()).isEqualTo(updateDto.getIata().toUpperCase());
            assertThat(responseBody.getName()).isEqualTo(updateDto.getName().toUpperCase());
            assertThat(responseBody.getCity()).isEqualTo(updateDto.getCity().toUpperCase());
            assertThat(responseBody.getCountry().getId()).isEqualTo(updateDto.getCountryId());
            assertThat(responseBody.getTimezone()).isEqualTo(updateDto.getTimezone());

            // assert persisted entity
            assertThat(airportAfterUpdate.getIcaoCode()).isEqualTo(referenceAirportBeforeUpdate.getIcaoCode());
            assertThat(airportAfterUpdate.getIataCode()).isEqualTo(updateDto.getIata().toUpperCase());
            assertThat(airportAfterUpdate.getName()).isEqualTo(updateDto.getName().toUpperCase());
            assertThat(airportAfterUpdate.getCity()).isEqualTo(updateDto.getCity().toUpperCase());
            assertThat(airportAfterUpdate.getCountry().getId()).isEqualTo(updateDto.getCountryId());
            assertThat(airportAfterUpdate.getTimezone()).isEqualTo(updateDto.getTimezone());
        }

        @Test
        @DisplayName("Should return airport not found exception when ICAO does not exist")
        void updateAirport_AirportNotFound_AirportNotFoundResponse() throws Exception {
            // given
            final String invalidIcaoCode = "fooo";

            // when & then
            performAndValidateUpdateException(
                    invalidIcaoCode,
                    updateDto,
                    HttpStatus.NOT_FOUND,
                    NOT_FOUND_ERROR_TITLE,
                    String.format(AIRPORT_NOT_FOUND_MESSAGE, invalidIcaoCode.toUpperCase())
            );
        }

        @Test
        @DisplayName("Should return conflict when new iata code already exists")
        void updateAirport_IataCodeAlreadyExists_ConflictResponse() throws Exception {
            // given
            final String newIataCode = "MUC";
            assertThat(airportRepository.existsByIataCode(newIataCode)).isTrue();
            updateDto.setIata(newIataCode);

            // when & then
            performAndValidateUpdateException(
                    referencedAirportIcao,
                    updateDto,
                    HttpStatus.CONFLICT,
                    CONFLICT_ERROR_TITLE,
                    String.format(AIRPORT_ALREADY_EXISTS, "IATA", newIataCode)
            );
        }

        @Test
        @DisplayName("Should return Bad Request when ICAO code is invalid")
        void updateAirport_InvalidIcaoCode_BadRequestResponse() throws Exception {
            // when & then
            performAndValidateUpdateException(
                    "1abc",
                    updateDto,
                    HttpStatus.BAD_REQUEST,
                    VALIDATION_ERROR_TITLE,
                    INVALID_ICAO_CODE_MESSAGE
            );
        }

        @Test
        @DisplayName("Should return CountryNotFound Exception when id does not exist")
        void updateAirport_CountryNotFound_CountryNotFoundResponse() throws Exception {
            // given
            updateDto.setCountryId(new UUID(0L,0L));

            // when & then
            performAndValidateUpdateException(
                    referencedAirportIcao,
                    updateDto,
                    HttpStatus.NOT_FOUND,
                    NOT_FOUND_ERROR_TITLE,
                    String.format(COUNTRY_NOT_FOUND_MESSAGE, updateDto.getCountryId())
            );
        }

        @Nested
        @DisplayName("DTO Validation")
        class DtoValidation {

            private Map<String, Object> payload;

            @BeforeEach
            void setUp() {
                payload = new HashMap<>(Map.of(
                        "iata", "ABC",
                        "name", "new name",
                        "city", "new City",
                        "countryId", CANADA_COUNTRY.getId().toString(),
                        "timezone", "America/Toronto"
                ));
            }

            /// A list of test scenarios with invalid AirportUpdateDto
            /// Argument Format: description, mutator, expected_error_message
            private static Stream<Arguments> invalidDtos() {
                return Stream.of(
                        Arguments.of("IATA is null", (GenericObjectMutator<AirportUpdateDto>) dto -> dto.setIata(null), MANDATORY_IATA_MISSING_MESSAGE),
                        Arguments.of("IATA is invalid", (GenericObjectMutator<AirportUpdateDto>) dto -> dto.setIata("abcde"), INVALID_IATA_CODE_MESSAGE),
                        Arguments.of("Name is null", (GenericObjectMutator<AirportUpdateDto>) dto -> dto.setName(null), MANDATORY_NAME_MISSING_MESSAGE),
                        Arguments.of("Name is blank", (GenericObjectMutator<AirportUpdateDto>) dto -> dto.setName(""), INVALID_NAME_MESSAGE),
                        Arguments.of("City is null", (GenericObjectMutator<AirportUpdateDto>) dto -> dto.setCity(null), MANDATORY_CITY_MISSING_MESSAGE),
                        Arguments.of("City is blank", (GenericObjectMutator<AirportUpdateDto>) dto -> dto.setCity(""), INVALID_CITY_CODE_MESSAGE),
                        Arguments.of("CountryId is null", (GenericObjectMutator<AirportUpdateDto>) dto -> dto.setCountryId(null), MANDATORY_COUNTRYID_MISSING_MESSAGE),
                        Arguments.of("Timezone is null", (GenericObjectMutator<AirportUpdateDto>) dto -> dto.setTimezone(null), MANDATORY_TIMEZONE_MISSING_MESSAGE),
                        Arguments.of("Timezone is blank", (GenericObjectMutator<AirportUpdateDto>) dto -> dto.setTimezone("foo bar"), String.format(INVALID_TIMEZONE_MESSAGE, "foo bar"))
                );
            }

            /// A list of test scenarios with missing fields in the request payload
            /// Argument Format: field_name, field_key, expected_error_message
            private static Stream<Arguments> missingFields() {
                return Stream.of(
                        Arguments.of("IATA", "iata", MANDATORY_IATA_MISSING_MESSAGE),
                        Arguments.of("name", "name", MANDATORY_NAME_MISSING_MESSAGE),
                        Arguments.of("city", "city", MANDATORY_CITY_MISSING_MESSAGE),
                        Arguments.of("countryId", "countryId", MANDATORY_COUNTRYID_MISSING_MESSAGE),
                        Arguments.of("timezone", "timezone", MANDATORY_TIMEZONE_MISSING_MESSAGE)
                );
            }

            @ParameterizedTest(name = "Should return 400 Bad Request when {0}")
            @MethodSource("invalidDtos")
            void createAirport_InvalidDto_ReturnBadRequest(Object ignored, GenericObjectMutator<AirportUpdateDto> mutator, String expectedMessage) throws Exception {
                // given
                mutator.accept(updateDto);

                // when & then
                performAndValidateDtoValidation(updateDto, expectedMessage);
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
                        performPutRequest(AirportsApi.PATH_UPDATE_AIRPORT_BY_ICAO, payload, referencedAirportIcao),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        expectedDetail,
                        dbCountBefore,
                        airportRepository::count
                );
            }
        }

        private void performAndValidateUpdateException(
                String icaoCode,
                Object body,
                HttpStatus httpStatus,
                String title, String detail
        ) throws Exception{
            performAndValidateException(
                    performPutRequest(AirportsApi.PATH_UPDATE_AIRPORT_BY_ICAO, body, icaoCode),
                    httpStatus,
                    title,
                    detail,
                    dbCountBefore,
                    airportRepository::count
            );

            final Airport airportAfterFailedUpdate = airportRepository.findById(referencedAirportIcao).orElse(null);
            assertThat(airportAfterFailedUpdate).isNotNull();

            /// Verifies the airport entity has not changed by comparing all existing attributes
            assertThat(airportAfterFailedUpdate)
                    .usingRecursiveComparison()
                    .ignoringFields("country")
                    .isEqualTo(referenceAirportBeforeUpdate);

            /// Verifies the country entity has not changed - must be seperated, as country is lazy loaded
            Assertions.assertNotNull(referenceAirportBeforeUpdate);
            assertThat(airportAfterFailedUpdate.getCountry().getId())
                    .isEqualTo(referenceAirportBeforeUpdate.getCountry().getId());
        }
    }
}
