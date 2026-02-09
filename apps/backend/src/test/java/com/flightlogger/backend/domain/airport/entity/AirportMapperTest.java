package com.flightlogger.backend.domain.airport.entity;

import com.flightlogger.backend.common.utils.StringFormatter;
import com.flightlogger.backend.domain.country.entity.CountryMapperImpl;
import com.flightlogger.backend.model.AirportReadDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT;
import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT_READ_DTO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = {AirportMapperImpl.class, CountryMapperImpl.class, StringFormatter.class})
class AirportMapperTest {

    @Autowired
    private AirportMapper mapper;

    @Test
    @DisplayName("Should map from entity to read dto")
    void toDto() {
        // when
        final AirportReadDto result = mapper.toDto(FRANKFURT_AIRPORT);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(FRANKFURT_AIRPORT_READ_DTO);
    }

}