package com.flightlogger.backend.domain.airline.service;

import com.flightlogger.backend.annotations.IntegrationTest;
import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.domain.airline.entity.AirlineRepository;
import com.flightlogger.backend.domain.airline.exception.AirlineAlreadyExistsException;
import com.flightlogger.backend.domain.airline.exception.AirlineNotFoundException;
import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import com.flightlogger.backend.model.AirlineUpdateDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.flightlogger.backend.testdata.AirlineTestData.CFG_READ_DTO;
import static com.flightlogger.backend.testdata.AirlineTestData.DLH_READ_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@IntegrationTest
class AirlineServiceImplIT {

    @Autowired
    private AirlineService airlineService;

    @Autowired
    private AirlineRepository airlineRepository;

    @Nested
    @DisplayName("GetAllAirlines")
    class GetAllAirlines {

        @Test
        @DisplayName("Should return all airlines from the database")
        void getAllAirlines_Success() {
            // when
            List<AirlineReadDto> airlines = airlineService.getAllAirlines();

            // then
            assertThat(airlines)
                    .hasSize(2)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(List.of(DLH_READ_DTO, CFG_READ_DTO));
        }
    }

    @Nested
    @DisplayName("GetAirlineByICAO")
    class GetAirlineByIcao {

        @Test
        @DisplayName("Should return desired airline by its icao code")
        void getAirlineByIcao_Success() {
            // when
            AirlineReadDto airline = airlineService.getAirlineByIcao(DLH_READ_DTO.getIcao());

            // then
            assertThat(airline).isEqualTo(DLH_READ_DTO);
        }

        @Test
        @DisplayName("Should throw AirlineNotFoundException when no airline was found")
        void getAirlineByIcao_NoAirlineWithSuchICAO_AirlineNotFoundException() {
            // when
            assertThatThrownBy(() -> airlineService.getAirlineByIcao("FOO"))
                    .isInstanceOf(AirlineNotFoundException.class)
                    .hasMessage("Airline with ICAO code FOO not found");
        }
    }

    @Nested
    @DisplayName("DeleteAirline")
    class DeleteAirline {

        @Test
        @DisplayName("Should delete airline from the database")
        void deleteAirlineByIcao_Success() {
            // given
            assertThat(airlineRepository.existsById(DLH_READ_DTO.getIcao())).isTrue();

            // when
            airlineService.deleteAirline(DLH_READ_DTO.getIcao().toLowerCase());

            // then
            assertThat(airlineRepository.existsById(DLH_READ_DTO.getIcao())).isFalse();
        }

        @Test
        @DisplayName("Should not throw an exception when icao code does not exist")
        void deleteAirlineByIcao_NoAirlineWithICAO_NoErrorThrown() {
            // given
            final String icaoCode = "FOO";
            long count = airlineRepository.count();
            assertThat(count).isGreaterThan(0);
            assertThat(airlineRepository.existsById(icaoCode)).isFalse();

            // when
            assertDoesNotThrow(() -> airlineService.deleteAirline(icaoCode));

            // then
            assertThat(airlineRepository.count()).isEqualTo(count);
        }
    }

    @Nested
    @DisplayName("SaveAirline")
    class SaveAirline {

        AirlineCreateDto newAirline;
        long dbCount;

        @BeforeEach
        void setUp() {
            dbCount = airlineRepository.count();
            newAirline = new AirlineCreateDto("aal", "aa", "America Airlines");
        }

        @Test
        @DisplayName("Should save new airline successfully")
        void saveAirline_Success() {
            // given
            assertThat(airlineRepository.existsById(newAirline.getIcao())).isFalse();

            // when
            AirlineReadDto savedAirline = airlineService.saveAirline(newAirline);

            // then
            assertThat(airlineRepository.existsById(savedAirline.getIcao())).isTrue();
            assertThat(savedAirline.getIcao()).isEqualTo(newAirline.getIcao().toUpperCase());
            assertThat(savedAirline.getIata()).isEqualTo(newAirline.getIata().toUpperCase());
            assertThat(savedAirline.getName()).isEqualTo(newAirline.getName());
            assertThat(savedAirline.getImageLink()).isNull();
        }

        @Test
        @DisplayName("Should throw AirlineAlreadyExistsException when icao code already exists")
        void saveAirline_IcaoExists_ThrowAirlineAlreadyExistsException() {
            // given
            newAirline.setIcao(DLH_READ_DTO.getIcao());

            // when and then
            assertThatThrownBy(() -> airlineService.saveAirline(newAirline))
                    .isInstanceOf(AirlineAlreadyExistsException.class)
                    .hasMessage("Airline with ICAO DLH already exists");
            assertThat(airlineRepository.count()).isEqualTo(dbCount);

        }

        @Test
        @DisplayName("Should throw AirlineAlreadyExistsException when iata code already exists")
        void saveAirline_IataExists_ThrowAirlineAlreadyExistsException() {
            // given
            newAirline.setIata(DLH_READ_DTO.getIata());

            // when and then
            assertThatThrownBy(() -> airlineService.saveAirline(newAirline))
                    .isInstanceOf(AirlineAlreadyExistsException.class)
                    .hasMessage("Airline with IATA LH already exists");
            assertThat(airlineRepository.count()).isEqualTo(dbCount);
        }
    }

    @Nested
    @DisplayName("UpdateAirline")
    class UpdateAirline {

        AirlineUpdateDto updateDto;

        @BeforeEach
        void setup() {
            updateDto = new AirlineUpdateDto();
            updateDto.setIata(DLH_READ_DTO.getIata());
            updateDto.setName(DLH_READ_DTO.getName());
        }

        @Test
        @DisplayName("Should update IATA and name successfully")
        void updateAirline_updateIataAndName_Success() {
            // given
            updateDto.setIata("aa");
            updateDto.setName("new name");
            Airline beforeUpdate = airlineRepository.findById(DLH_READ_DTO.getIcao()).orElse(null);
            assertThat(beforeUpdate).isNotNull();
            assertThat(beforeUpdate.getIataCode()).isNotEqualTo(updateDto.getIata().toUpperCase());
            assertThat(beforeUpdate.getName()).isNotEqualTo(updateDto.getName());

            // when
            AirlineReadDto result = airlineService.updateAirline(DLH_READ_DTO.getIcao().toLowerCase(), updateDto);
            Airline afterUpdate = airlineRepository.findById(DLH_READ_DTO.getIcao()).orElse(null);

            // then
            assertThat(result.getIata()).isEqualTo(updateDto.getIata().toUpperCase());
            assertThat(result.getName()).isEqualTo(updateDto.getName());

            assertThat(afterUpdate).isNotNull();
            assertThat(afterUpdate.getIataCode()).isEqualTo(updateDto.getIata().toUpperCase());
            assertThat(afterUpdate.getName()).isEqualTo(updateDto.getName());
            assertThat(afterUpdate.getImageLink()).isEqualTo(beforeUpdate.getImageLink());
        }

        @Test
        @DisplayName("Should set image link null")
        void updateAirline_updateImageLink_Success() {
            // given
            updateDto.setImageLink(null);
            Airline beforeUpdate = airlineRepository.findById(DLH_READ_DTO.getIcao()).orElse(null);

            Assertions.assertNotNull(beforeUpdate);
            assertThat(beforeUpdate.getImageLink()).isNotNull();

            // when
            AirlineReadDto result = airlineService.updateAirline(DLH_READ_DTO.getIcao().toLowerCase(), updateDto);
            Airline afterUpdate = airlineRepository.findById(DLH_READ_DTO.getIcao()).orElse(null);

            // then
            assertThat(result.getImageLink()).isNull();
            assertThat(afterUpdate).isNotNull();
            assertThat(afterUpdate.getImageLink()).isNull();
        }

        @Test
        @DisplayName("Should throw AirlineAlreadyExistsException when new IATA already exists")
        void updateAirline_IataExists_ThrowAirlineAlreadyExistsException() {
            // given
            updateDto.setIata(CFG_READ_DTO.getIata());

            // when & then
            assertThatThrownBy(() -> airlineService.updateAirline(DLH_READ_DTO.getIcao(), updateDto))
                    .isInstanceOf(AirlineAlreadyExistsException.class)
                    .hasMessage("Airline with IATA DE already exists");
        }

        @Test
        @DisplayName("Should throw AirlineNotFoundException when no airline found for ICAO")
        void updateAirline_IcaoDoesNotExist_ThrowAirlineNotFoundException() {
            // when & then
            assertThatThrownBy(() -> airlineService.updateAirline("FOO", updateDto))
                    .isInstanceOf(AirlineNotFoundException.class)
                    .hasMessage("Airline with ICAO code FOO not found");
        }
    }
}