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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

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
}
