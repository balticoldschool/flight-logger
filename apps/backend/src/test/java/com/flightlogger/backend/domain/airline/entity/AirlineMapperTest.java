package com.flightlogger.backend.domain.airline.entity;

import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import com.flightlogger.backend.model.AirlineUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.flightlogger.backend.testdata.AirlineTestData.*;
import static org.assertj.core.api.Assertions.assertThat;


class AirlineMapperTest {

    private AirlineMapper mapper = new AirlineMapperImpl();

    @Test
    @DisplayName("Should map Airline entity to AirlineReadDto")
    void toDto() {
        // when
        AirlineReadDto dto = mapper.toDto(DLH_AIRLINE);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto).isEqualTo(DLH_READ_DTO);
    }

    @Test
    @DisplayName("Should map AirlineCreateDto to Airline entity")
    void toEntity() {
        // given
        AirlineCreateDto dto = CFG_CREATE_DTO;
        dto.setImageLink(CFG_AIRLINE.getImageLink());

        // when
        Airline airline = mapper.toEntity(dto);

        // then
        assertThat(airline).isNotNull();
        assertThat(airline)
                .usingRecursiveComparison()
                .isEqualTo(CFG_AIRLINE);
    }

    @Test
    @DisplayName("Should only update given attributes and set missing ones to null")
    void updateFromDto() {
        // given
        final String icaoCode = "DLH";
        Airline airline = new Airline(icaoCode, "LH", "old name", "some link");
        AirlineUpdateDto dto = new AirlineUpdateDto();
        dto.setIata("aa");
        dto.setName("new name");

        // when
        mapper.updateFromDto(dto, airline);

        // then
        assertThat(airline.getIcaoCode()).isEqualTo(icaoCode);
        assertThat(airline.getIataCode()).isEqualTo(dto.getIata().toUpperCase());
        assertThat(airline.getName()).isEqualTo(dto.getName());
        assertThat(airline.getImageLink()).isNull();
    }
}