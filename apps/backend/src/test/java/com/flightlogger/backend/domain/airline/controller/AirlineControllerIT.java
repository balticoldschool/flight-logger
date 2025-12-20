package com.flightlogger.backend.domain.airline.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flightlogger.backend.config.BaseControllerIT;
import com.flightlogger.backend.domain.airline.entity.AirlineRepository;
import com.flightlogger.backend.model.AirlineReadDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static com.flightlogger.backend.testdata.AirlineTestData.CFG_READ_DTO;
import static com.flightlogger.backend.testdata.AirlineTestData.DLH_READ_DTO;
import static org.assertj.core.api.Assertions.assertThat;

class AirlineControllerIT extends BaseControllerIT {

    @Autowired
    private AirlineRepository airlineRepository;

    final String BASE_URL = "/airlines";

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
            List<AirlineReadDto> airlines = readResponseBody(response, new TypeReference<List<AirlineReadDto>>() {});

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
            String url = BASE_URL.concat("/").concat(DLH_READ_DTO.getIcao());

            // when
            MockHttpServletResponse response = performGetRequest(url);
            AirlineReadDto airline = readResponseBody(response, AirlineReadDto.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(airline).isEqualTo(DLH_READ_DTO);
        }

        @Test
        @DisplayName("Should return 404 with corresponding message")
        void getAirlineByIcao_AirlineNotFound_ReturnNotFoundResponse() throws Exception {
            // given
            String url = BASE_URL.concat("/").concat("foo");

            // when
            MockHttpServletResponse response = performGetRequest(url);
            ProblemDetail problemDetail = readResponseBody(response, ProblemDetail.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
            assertThat(problemDetail.getDetail()).isEqualTo("Airline with ICAO code FOO not found");
        }

        @Test
        @DisplayName("Should return 400 bad request with corresponding message")
        void getAirlineByIcao_InvalidIcaoCode_ReturnBadRequest() throws Exception {
            // given
            String url = BASE_URL.concat("/").concat("a");

            // when
            MockHttpServletResponse response = performGetRequest(url);
            ProblemDetail problemDetail = readResponseBody(response, ProblemDetail.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(problemDetail.getDetail()).isEqualTo("invalid ICAO code");
        }
    }
}