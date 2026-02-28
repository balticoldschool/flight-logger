package com.flightlogger.backend.domain.airport.service;

import com.flightlogger.backend.annotations.IntegrationTest;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.domain.airport.entity.AirportRepository;
import com.flightlogger.backend.domain.airport.exception.AirportNotFoundException;
import com.flightlogger.backend.model.AirportReadDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT_READ_DTO;
import static com.flightlogger.backend.testdata.ErrorMessages.AIRPORT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
class AirportServiceImplIT {

    @Autowired
    private AirportService airportService;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private AirportMapper airportMapper;

    private Long countBeforeUpdate;

    @BeforeEach
    void setUp() {
        // prevents unnecessary database queries
        if (countBeforeUpdate == null) {
            countBeforeUpdate = airportRepository.count();
        }
    }

    @Nested
    @DisplayName("getAllAirports")
    class GetAllAirports {

        @Test
        @DisplayName("Should return all airports from the database")
        void getAllAirports_Success() {
            // given
            final List<AirportReadDto> expectedAirports =
                    airportRepository.findAll().stream().map(airportMapper::toDto).toList();

            // when
            List<AirportReadDto> airports = airportService.getAllAirports();

            // then
            assertThat(airports)
                    .hasSize(countBeforeUpdate.intValue())
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(expectedAirports);
        }
    }

    @Nested
    @DisplayName("getAirportByIata")
    class GetAirportByIata {

        @Test
        @DisplayName("Should return desired airport by its ICAO code")
        void getAirportById_Success() {
            // when
            final AirportReadDto result = airportService.getAirportByIcao(FRANKFURT_AIRPORT_READ_DTO.getIcao());

            // then
            assertThat(result).isEqualTo(FRANKFURT_AIRPORT_READ_DTO);
        }

        @Test
        @DisplayName("Should return AirportNotFound Exception when invalid icao code was given")
        void getAirportById_InvalidIcaoCode_AirportNotFoundException() {
            // given
            final String invalidIcao = "INVALID";

            // when & then
            assertThatThrownBy(() -> airportService.getAirportByIcao(invalidIcao))
                    .isInstanceOf(AirportNotFoundException.class)
                    .hasMessageContaining(String.format(AIRPORT_NOT_FOUND, invalidIcao));
        }
    }

}