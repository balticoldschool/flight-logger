package com.flightlogger.backend.domain.airport.service;

import com.flightlogger.backend.annotations.IntegrationTest;
import com.flightlogger.backend.common.exception.InvalidTimeZoneException;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.domain.airport.entity.AirportRepository;
import com.flightlogger.backend.domain.airport.exception.AirportAlreadyExistsException;
import com.flightlogger.backend.domain.airport.exception.AirportNotFoundException;
import com.flightlogger.backend.domain.country.exception.CountryNotFoundException;
import com.flightlogger.backend.model.AirportCreateDto;
import com.flightlogger.backend.model.AirportReadDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT;
import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT_READ_DTO;
import static com.flightlogger.backend.testdata.CountryTestData.GERMANY_COUNTRY;
import static com.flightlogger.backend.testdata.ErrorMessages.*;
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
                    .hasMessageContaining(String.format(AIRPORT_NOT_FOUND_MESSAGE, invalidIcao));
        }
    }

    @Nested
    @DisplayName("saveAirport")
    class SaveAirport {

        private AirportCreateDto createDto;

        @BeforeEach
        void setUp() {
            createDto = new AirportCreateDto()
                    .icao("etnl")
                    .iata("rlg")
                    .name("Rotock Laage")
                    .city("Rostock")
                    .countryId(GERMANY_COUNTRY.getId())
                    .timezone("Europe/Berlin");
        }

        @Test
        @DisplayName("Should save new airport successfully")
        void saveAirport_Success() {
            // given
            assertThat(airportRepository.existsById(createDto.getIcao().toUpperCase())).isFalse();
            assertThat(airportRepository.existsByIataCode(createDto.getIata().toUpperCase())).isFalse();

            // when
            AirportReadDto savedAirport = airportService.saveAirport(createDto);

            // then
            assertThat(airportRepository.count()).isEqualTo(countBeforeUpdate + 1);
            assertThat(airportRepository.existsById(createDto.getIcao().toUpperCase())).isTrue();
            assertThat(airportRepository.existsByIataCode(createDto.getIata().toUpperCase())).isTrue();

            assertThat(savedAirport.getIcao()).isEqualTo(createDto.getIcao().toUpperCase());
            assertThat(savedAirport.getIata()).isEqualTo(createDto.getIata().toUpperCase());
            assertThat(savedAirport.getName()).isEqualTo(createDto.getName().toUpperCase());
            assertThat(savedAirport.getCity()).isEqualTo(createDto.getCity().toUpperCase());
            assertThat(savedAirport.getTimezone()).isEqualTo(createDto.getTimezone().toUpperCase());

            assertThat(savedAirport.getCountry().getId()).isEqualTo(createDto.getCountryId());
        }

        @Test
        @DisplayName("Should throw AirportAlreadyExistsException when ICAO code already exists")
        void saveAirport_IcaoExists_ThrowAirportAlreadyExistsException() {
            // given
            createDto.setIcao(FRANKFURT_AIRPORT.getIcaoCode());

            // when & then
            assertThatThrownBy(() -> airportService.saveAirport(createDto))
                    .isInstanceOf(AirportAlreadyExistsException.class)
                    .hasMessageContaining(String.format(AIRPORT_ALREADY_EXISTS, "ICAO", createDto.getIcao()));

            assertThat(airportRepository.count()).isEqualTo(countBeforeUpdate);
        }

        @Test
        @DisplayName("Should throw AirportAlreadyExistsException when IATA code already exists")
        void saveAirport_IataExists_ThrowAirportAlreadyExistsException() {
            // given
            createDto.setIata(FRANKFURT_AIRPORT.getIataCode());

            // when & then
            assertThatThrownBy(() -> airportService.saveAirport(createDto))
                    .isInstanceOf(AirportAlreadyExistsException.class)
                    .hasMessageContaining(String.format(AIRPORT_ALREADY_EXISTS, "IATA", createDto.getIata()));

            assertThat(airportRepository.count()).isEqualTo(countBeforeUpdate);
        }

        @Test
        @DisplayName("Should throw InvalidTimeZoneException when timezone is invalid")
        void saveAirport_InvalidTimezone_ThrowInvalidTimeZoneException() {
            // given
            createDto.setTimezone("Invalid/Timezone");

            // when & then
            assertThatThrownBy(() -> airportService.saveAirport(createDto))
                    .isInstanceOf(InvalidTimeZoneException.class)
                    .hasMessageContaining(String.format(INVALID_TIMEZONE_MESSAGE, createDto.getTimezone()));

            assertThat(airportRepository.count()).isEqualTo(countBeforeUpdate);
        }

        @Test
        @DisplayName("Should throw CountryNotFoundException when countryId does not exist")
        void saveAirport_CountryNotFound_ThrowCountryNotFoundException() {
            // given
            UUID randomId = UUID.randomUUID();
            createDto.setCountryId(randomId);

            // when & then
            assertThatThrownBy(() -> airportService.saveAirport(createDto))
                    .isInstanceOf(CountryNotFoundException.class)
                    .hasMessage(String.format(COUNTRY_NOT_FOUND_MESSAGE, randomId));

            assertThat(airportRepository.count()).isEqualTo(countBeforeUpdate);
        }
    }
}