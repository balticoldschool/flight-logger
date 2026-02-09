package com.flightlogger.backend.domain.airport.service;

import com.flightlogger.backend.annotations.IntegrationTest;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.domain.airport.entity.AirportRepository;
import com.flightlogger.backend.model.AirportReadDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}