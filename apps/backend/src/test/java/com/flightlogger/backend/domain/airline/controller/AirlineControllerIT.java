package com.flightlogger.backend.domain.airline.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flightlogger.backend.config.BaseControllerIT;
import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.domain.airline.entity.AirlineRepository;
import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import com.flightlogger.backend.model.AirlineUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Map;

import static com.flightlogger.backend.testdata.AirlineTestData.CFG_READ_DTO;
import static com.flightlogger.backend.testdata.AirlineTestData.DLH_READ_DTO;
import static com.flightlogger.backend.testdata.ErrorMessages.*;
import static org.assertj.core.api.Assertions.assertThat;

class AirlineControllerIT extends BaseControllerIT {

    @Autowired
    private AirlineRepository airlineRepository;

    final String BASE_URL = "/airlines";

    long dbCountBefore;

    @BeforeEach
    void setUp() {
        dbCountBefore = airlineRepository.count();
    }

    @Nested
    @DisplayName("Get all airlines")
    class GetAllAirlines {

        @Test
        @DisplayName("Should return a list of airlines")
        void getAllAirlines_Success() throws Exception {
            // given
            long count = airlineRepository.count();
            assertThat(count).isGreaterThan(0);

            // when
            MockHttpServletResponse response = performGetRequest(BASE_URL);
            List<AirlineReadDto> airlines = readResponseBody(response, new TypeReference<>() {
            });

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(airlines.size()).isEqualTo(count);
            assertThat(airlines).contains(DLH_READ_DTO, CFG_READ_DTO);
        }
    }

    @Nested
    @DisplayName("Get airline by ICAO")
    class GetAirlineByIcao {

        @Test
        @DisplayName("Should return the desired airline")
        void getAirlineByIcao_Success() throws Exception {
            // given
            String icaoCode = DLH_READ_DTO.getIcao();

            // when
            MockHttpServletResponse response = performGetRequest(BASE_URL + "/{icao}", icaoCode.toLowerCase());
            AirlineReadDto airline = readResponseBody(response, AirlineReadDto.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(airline).isEqualTo(DLH_READ_DTO);
        }

        @Test
        @DisplayName("Should return 404 with corresponding message")
        void getAirlineByIcao_AirlineNotFound_ReturnNotFoundResponse() throws Exception {
            // given
            String invalidIcaoCode = "foo";

            // when
            performAndValidateException(
                    performGetRequest(BASE_URL + "/{icao}", invalidIcaoCode),
                    HttpStatus.NOT_FOUND,
                    NOT_FOUND_ERROR_TITLE,
                    String.format(AIRLINE_NOT_FOUND, invalidIcaoCode.toUpperCase())
                    );
        }

        @Test
        @DisplayName("Should return 400 bad request with corresponding message")
        void getAirlineByIcao_InvalidIcaoCode_ReturnBadRequest() throws Exception {
            // when
            performAndValidateException(
                    performGetRequest(BASE_URL + "/{icao}", "1abc"),
                    HttpStatus.BAD_REQUEST,
                    BAD_REQUEST_ERROR_TITLE,
                    INVALID_ICAO_CODE_MESSAGE
            );
        }
    }

    @Nested
    @DisplayName("Delete airline")
    class DeleteAirline {

        long dbCount;

        @BeforeEach
        void setUp() {
            dbCount = airlineRepository.count();
        }

        @Test
        @DisplayName("Should delete airline and return status 204")
        void deleteAirline_Success() throws Exception {
            // given
            String icaoCode = DLH_READ_DTO.getIcao();
            assertThat(airlineRepository.existsById(icaoCode)).isTrue();

            // when
            MockHttpServletResponse response = performDeleteRequest(BASE_URL + "/{icao}", icaoCode.toLowerCase());

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(response.getContentAsString()).isEmpty();
            assertThat(airlineRepository.existsById(DLH_READ_DTO.getIcao())).isFalse();
        }

        @Test
        @DisplayName("Should simply return 204 no content when icao code does not exist")
        void deleteAirline_IcaoDoesNotExist_Success() throws Exception {
            // given
            String icaoCode = "FOO";
            assertThat(airlineRepository.existsById(icaoCode)).isFalse();

            // when
            MockHttpServletResponse response = performDeleteRequest(BASE_URL + "/{icao}", icaoCode);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(airlineRepository.count()).isEqualTo(dbCount);
        }

        @Test
        @DisplayName("Should throw 400 Bad Request when icao is invalid")
        void deleteAirline_InvalidIcao_ThrowBadRequest() throws Exception {
            // when
            MockHttpServletResponse response = performDeleteRequest(BASE_URL + "/{icao}", "a");
            ProblemDetail problemDetail = readResponseBody(response, ProblemDetail.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
            assertThat(problemDetail.getDetail()).isEqualTo(INVALID_ICAO_CODE_MESSAGE);
        }
    }

    @Nested
    @DisplayName("Create airline")
    class CreateAirline {

        AirlineCreateDto newAirline;

        @BeforeEach
        void setUp() {
            newAirline = new AirlineCreateDto("aal", "aa", "American Airlines");
        }

        @Test
        @DisplayName("Should create new airline and return 201")
        void createAirline_Success() throws Exception {
            // given
            newAirline.setImageLink("https://foo.bar/image.png");

            // when
            MockHttpServletResponse response = performPostRequest(BASE_URL, newAirline);
            AirlineReadDto createAirline = readResponseBody(response, AirlineReadDto.class);
            long dbCountAfter = airlineRepository.count();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(dbCountAfter).isEqualTo(dbCountBefore + 1);
            assertThat(airlineRepository.existsById(newAirline.getIcao().toUpperCase())).isTrue();

            assertThat(createAirline.getIcao()).isEqualTo(newAirline.getIcao().toUpperCase());
            assertThat(createAirline.getIata()).isEqualTo(newAirline.getIata().toUpperCase());
            assertThat(createAirline.getName()).isEqualTo(newAirline.getName());
            assertThat(createAirline.getImageLink()).isEqualTo(newAirline.getImageLink());
        }

        @Test
        @DisplayName("Should create new airline without image link and return 201")
        void createAirline_NoImageLink_Success() throws Exception {
            // when
            MockHttpServletResponse response = performPostRequest(BASE_URL, newAirline);
            AirlineReadDto createAirline = readResponseBody(response, AirlineReadDto.class);
            long dbCountAfter = airlineRepository.count();

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(dbCountAfter).isEqualTo(dbCountBefore + 1);
            assertThat(createAirline.getImageLink()).isNull();
        }

        @Nested
        @DisplayName("DTO validation exceptions - should not create airline and return 400 Bad Request -")
        class DtoValidation {

            @Test
            @DisplayName("when icao is missing")
            void createAirline_MissingIcao_ReturnBadRequest() throws Exception {
                // given
                Map<String, String> invalidPayload = Map.of(
                        "iata", "lh",
                        "name", "Lufthansa"
                );

                // when & then
                performAndValidateDtoValidation(invalidPayload, MANDATORY_ICAO_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("when icao is null")
            void createAirline_IcaoNull_ReturnBadRequest() throws Exception {
                // given
                newAirline.setIcao(null);

                // when & then
                performAndValidateDtoValidation(newAirline, MANDATORY_ICAO_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("when icao is invalid")
            void createAirline_InvalidIcao_ReturnBadRequest() throws Exception {
                // given
                newAirline.setIcao("1abc");

                // when & then
                performAndValidateDtoValidation(newAirline, INVALID_ICAO_CODE_MESSAGE);
            }

            @Test
            @DisplayName("when iata is missing")
            void createAirline_MissingIata_ReturnBadRequest() throws Exception {
                // given
                Map<String, String> invalidPayload = Map.of(
                        "icao", "DLH",
                        "name", "Deutsche Lufthansa"
                );

                // when & then
                performAndValidateDtoValidation(invalidPayload, MANDATORY_IATA_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("when iata is null")
            void createAirline_IataNull_ReturnBadRequest() throws Exception {
                // given
                newAirline.setIata(null);

                // when & then
                performAndValidateDtoValidation(newAirline, MANDATORY_IATA_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("when iata is invalid")
            void createAirline_InvalidIata_ReturnBadRequest() throws Exception {
                // given
                newAirline.setIata("abcde");

                // when & then
                performAndValidateDtoValidation(newAirline, INVALID_IATA_CODE_MESSAGE);
            }

            @Test
            @DisplayName("when name is missing")
            void createAirline_NameMissing_ReturnBadRequest() throws Exception {
                // given
                Map<String, String> invalidPayload = Map.of(
                        "icao", "DLH",
                        "iata", "LH"
                );

                // when & then
                performAndValidateDtoValidation(invalidPayload, MANDATORY_NAME_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("when name is null")
            void createAirline_NameNull_RetrurnBadRequest() throws Exception {
                // given
                newAirline.setName(null);

                // when & then
                performAndValidateDtoValidation(newAirline, MANDATORY_NAME_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("when name is invalid")
            void createAirline_NameInvalid_ReturnBadRequest() throws Exception {
                // given
                newAirline.setName("");

                // when & then
                performAndValidateDtoValidation(newAirline, INVALID_NAME_MESSAGE);
            }

            private void performAndValidateDtoValidation(Object payload, String expectedDetail) throws Exception {
                performAndValidateException(
                        performPostRequest(BASE_URL, payload),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        expectedDetail);
            }
        }

        @Nested
        @DisplayName("Conflicts - should not create airline and return 409 Conflict -")
        class Conflicts {

            @Test
            @DisplayName("when icao code exists")
            void createAirline_IcaoExists_ReturnConflict() throws Exception {
                // given
                String lufthansaIcao = DLH_READ_DTO.getIcao();
                newAirline.setIcao(lufthansaIcao.toUpperCase());
                String expectedProblemDetail =
                        String.format(AIRLINE_ICAO_ALREADY_EXISTS_MESSAGE, DLH_READ_DTO.getIcao());

                // when & then
                performAndValidateConflict(newAirline, expectedProblemDetail);
            }

            @Test
            @DisplayName("when iata code exists")
            void createAirline_IataExists_ReturnConflict() throws Exception {
                // given
                String existingIata = CFG_READ_DTO.getIata();
                newAirline.setIata(existingIata);
                String expectedDetailsMessage = String.format(AIRLINE_IATA_ALREADY_EXISTS_MESSAGE, existingIata);

                // when & then
                performAndValidateConflict(newAirline, expectedDetailsMessage);
            }

            private void performAndValidateConflict(Object payload, String expectedDetail) throws Exception {
                performAndValidateException(
                        performPostRequest(BASE_URL, payload),
                        HttpStatus.CONFLICT,
                        CONFLICT_ERROR_TITLE,
                        expectedDetail
                );
            }
        }
    }

    @Nested
    @DisplayName("Update airline")
    class UpdateAirline {
        AirlineUpdateDto updateDto;
        String referenceAirlineIcao = DLH_READ_DTO.getIcao();
        Airline referenceAirlineBeforeUpdate = airlineRepository.findById(referenceAirlineIcao).orElse(null);

        @BeforeEach
        void setUp() {
            assertThat(referenceAirlineBeforeUpdate).isNotNull();
            updateDto = new AirlineUpdateDto("aa", "Updated Airline");
        }

        @Test
        @DisplayName("Should update airline and return http status OK")
        void updateAirline_Success() throws Exception {
            // given
            assertThat(referenceAirlineBeforeUpdate.getName()).isNotEqualTo(updateDto.getName());
            assertThat(referenceAirlineBeforeUpdate.getIataCode()).isNotEqualTo(updateDto.getIata().toUpperCase());

            // when
            MockHttpServletResponse response =
                    performPutRequest(BASE_URL + "/{icao}", updateDto, "DLH");
            AirlineReadDto responseBody = readResponseBody(response, AirlineReadDto.class);
            Airline airlineAfterUpdate = airlineRepository.findById(referenceAirlineIcao).orElse(null);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(airlineAfterUpdate).isNotNull();
            assertThat(responseBody.getIcao()).isEqualTo(referenceAirlineIcao.toUpperCase());
            assertThat(responseBody.getIata()).isEqualTo(updateDto.getIata().toUpperCase());
            assertThat(responseBody.getName()).isEqualTo(updateDto.getName());
            assertThat(responseBody.getImageLink()).isEqualTo(updateDto.getImageLink());

            assertThat(airlineAfterUpdate.getIcaoCode()).isEqualTo(referenceAirlineBeforeUpdate.getIcaoCode());
            assertThat(airlineAfterUpdate.getIataCode()).isEqualTo(updateDto.getIata().toUpperCase());
            assertThat(airlineAfterUpdate.getName()).isEqualTo(updateDto.getName());
            assertThat(airlineAfterUpdate.getImageLink()).isEqualTo(updateDto.getImageLink());
        }

        @Test
        @DisplayName("Should return airline not found when icao code does not exist")
        void updateAirline_AirlineNotFound_AirlineNotFoundResponse() throws Exception {
            // given
            final String invalidIcao = "foo";

            // when & then
            performAndValidateUpdateException(
                    invalidIcao,
                    updateDto,
                    HttpStatus.NOT_FOUND,
                    NOT_FOUND_ERROR_TITLE,
                    String.format(AIRLINE_NOT_FOUND, invalidIcao.toUpperCase()));
        }

        @Test
        @DisplayName("Should return conflict when desired iata code already exists")
        void updateAirline_IataCodeExists_ReturnsConflict() throws Exception {
            // given
            updateDto.setIata(CFG_READ_DTO.getIata());

            // when & then
            performAndValidateUpdateException(
                    referenceAirlineIcao,
                    updateDto,
                    HttpStatus.CONFLICT,
                    CONFLICT_ERROR_TITLE,
                    String.format(AIRLINE_IATA_ALREADY_EXISTS_MESSAGE, updateDto.getIata().toUpperCase())
            );

        }

        @Test
        @DisplayName("Should return Bad Request when ICAO code is invalid")
        void updateAirline_InvalidIcao_ReturnBadRequest() throws Exception {
            // given
            String invalidIcao = "1abc";

            // when & then
            performAndValidateUpdateException(
                    invalidIcao,
                    updateDto,
                    HttpStatus.BAD_REQUEST,
                    BAD_REQUEST_ERROR_TITLE,
                    INVALID_ICAO_CODE_MESSAGE
            );
        }


        @Nested
        @DisplayName("Validation - should not update airline and return 400 Bad Request when -")
        class Validation {

            @Test
            @DisplayName("when IATA code is missing")
            void updateAirline_MissingIata_ReturnBadRequest() throws Exception {
                // given
                Map<String, String> invalidPayload = Map.of(
                  "name", "Updated Airline Name"
                );

                // when & then
                performAndValidateUpdateValidation(referenceAirlineIcao, invalidPayload, MANDATORY_IATA_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("when IATA code is null")
            void updateAirline_IataNull_ReturnBadRequest() throws Exception {
                // given
                updateDto.setIata(null);

                // when & then
                performAndValidateUpdateValidation(referenceAirlineIcao, updateDto, MANDATORY_IATA_MISSING_MESSAGE);
            }

            // invalid iata
            @Test
            @DisplayName("when IATA code is invalid")
            void updateAirline_InvalidIata_ReturnBadRequest() throws Exception {
                // given
                updateDto.setIata("abcde");

                // when & then
                performAndValidateUpdateValidation(referenceAirlineIcao, updateDto, INVALID_IATA_CODE_MESSAGE);
            }

            // missing name

            @Test
            @DisplayName("when name is missing")
            void updateAirline_MissingName_ReturnBadRequest() throws Exception {
                // given
                Map<String, String> invalidPayload = Map.of(
                        "iata", "aa"
                );

                // when & then
                performAndValidateUpdateValidation(referenceAirlineIcao, invalidPayload, MANDATORY_NAME_MISSING_MESSAGE);
            }

            // name is blank
            @Test
            @DisplayName("when name is blank")
            void updateAirline_NameIsBlank_ReturnBadRequest() throws Exception {
                // given
                updateDto.setName("");

                // when & then
                performAndValidateUpdateValidation(referenceAirlineIcao, updateDto, INVALID_NAME_MESSAGE);
            }

            private void performAndValidateUpdateValidation(String icaoCode, Object requestBody, String detail) throws Exception {
                performAndValidateUpdateException(
                        icaoCode,
                        requestBody,
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        detail
                );
            }
        }

        private void performAndValidateUpdateException(String icaoCode, Object body, HttpStatus httpStatus, String title, String detail) throws Exception {
            performAndValidateException(
                    performPutRequest(BASE_URL + "/{icao}", body, icaoCode),
                    httpStatus,
                    title,
                    detail
            );

            final Airline airlineAfterFailedUpdate = airlineRepository.findById(referenceAirlineIcao).orElse(null);
            final long dbCountAfterFailedUpdate = airlineRepository.count();

            assertThat(airlineAfterFailedUpdate).isNotNull();
            assertThat(dbCountAfterFailedUpdate).isEqualTo(dbCountBefore);

            ///  Verifies the airline entity has not changed by comparing all existing attributes
            assertThat(airlineAfterFailedUpdate)
                    .usingRecursiveComparison()
                    .isEqualTo(referenceAirlineBeforeUpdate);
        }
    }

    private void performAndValidateException(
            MockHttpServletResponse response,
            HttpStatus expectedStatus,
            String expectedTitle,
            String expectedDetail) throws Exception {
        // when
        ProblemDetail problemDetail = readResponseBody(response, ProblemDetail.class);
        long dbCountAfter = airlineRepository.count();

        // then
        assertThat(response.getStatus()).isEqualTo(expectedStatus.value());
        assertThat(dbCountAfter).isEqualTo(dbCountBefore);
        assertThat(problemDetail.getTitle()).isEqualTo(expectedTitle);
        assertThat(problemDetail.getDetail()).isEqualTo(expectedDetail);
    }
}