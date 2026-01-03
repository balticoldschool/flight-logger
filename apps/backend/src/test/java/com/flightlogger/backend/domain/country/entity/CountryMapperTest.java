package com.flightlogger.backend.domain.country.entity;

import com.flightlogger.backend.model.CountryReadDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.flightlogger.backend.testdata.CountryTestData.CANADA_COUNTRY;
import static com.flightlogger.backend.testdata.CountryTestData.CANADA_READ_DTO;
import static org.assertj.core.api.Assertions.assertThat;

class CountryMapperTest {

    private final CountryMapper mapper = new CountryMapperImpl();

    @Test
    @DisplayName("Should map Country entity to CountryReadDto")
    void toDto() {
        // when
        CountryReadDto dto = mapper.toDto(CANADA_COUNTRY);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto).usingRecursiveComparison().isEqualTo(CANADA_READ_DTO);
    }
}