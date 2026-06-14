package com.flightlogger.backend.domain.flight.service;

import com.flightlogger.backend.annotations.IntegrationTest;
import com.flightlogger.backend.domain.flight.entity.FlightMapper;
import com.flightlogger.backend.domain.flight.entity.FlightRepository;
import com.flightlogger.backend.model.FlightReadDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Loads the flight data - this is not part of liquibase to avoid FK constraint violations
@Sql(scripts = "classpath:db/test/flight_test_data.sql")
@IntegrationTest
class FlightServiceImplTestIT {

    // Mirrors flight_test_data.sql — update constants here when seed data changes
    private static final long SEED_FLIGHT_COUNT = 2L;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightMapper flightMapper;

    @Nested
    @DisplayName("getAllFlights")
    class GetAllFlights {

        @Test
        @DisplayName("Should return all seeded flights on the first page")
        void getAllFlights_returnsAllFlightsOnFirstPage() {
            // given
            int page = 0;
            int pageSize = (int) SEED_FLIGHT_COUNT;
            final List<FlightReadDto> expectedFlights = flightRepository
                    .findAll()
                    .stream()
                    .map(flightMapper::toDto)
                    .toList();

            // when
            final Page<FlightReadDto> result = flightService.getAllFlights(page, pageSize);

            // then
            assertThat(result.getContent())
                    .hasSize((int) SEED_FLIGHT_COUNT)
                    .containsExactlyInAnyOrderElementsOf(expectedFlights);
            assertPageMetadata(result, pageSize, page);
        }

        @Test
        @DisplayName("Should return the correct slice on the second page")
        void getAllFlights_returnsCorrectSecondPage() {
            // given
            int page = (int) SEED_FLIGHT_COUNT - 1; // last page
            int pageSize = 1;
            final FlightReadDto expectedFlight = flightMapper.toDto(
                    flightRepository.findAll().getLast()
            );

            // when
            Page<FlightReadDto> result = flightService.getAllFlights(page, pageSize);

            // then
            assertThat(result.getContent())
                    .hasSize(1).isEqualTo(List.of(expectedFlight));
            assertPageMetadata(result, pageSize, page);
        }

        @Test
        @DisplayName("Should return empty content when page index is out of bounds")
        void getAllFlights_returnsEmptyContentForOutOfBoundsPage() {
            // given
            int page = 1;
            int pageSize = (int) SEED_FLIGHT_COUNT + 1;

            // when
            Page<FlightReadDto> result = flightService.getAllFlights(page, pageSize);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(SEED_FLIGHT_COUNT);
        }

        private void assertPageMetadata(Page<?> page, int expectedPageSize, int expectedPageNumber) {
            int expectedTotalPages = (int) Math.ceil((double) SEED_FLIGHT_COUNT / expectedPageSize);

            assertThat(page.getTotalElements()).isEqualTo(SEED_FLIGHT_COUNT);
            assertThat(page.getTotalPages()).isEqualTo(expectedTotalPages);
            assertThat(page.getNumber()).isEqualTo(expectedPageNumber);
            assertThat(page.getSize()).isEqualTo(expectedPageSize);
        }
    }
}