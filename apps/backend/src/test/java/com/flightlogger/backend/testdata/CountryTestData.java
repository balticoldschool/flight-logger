package com.flightlogger.backend.testdata;

import com.flightlogger.backend.domain.country.entity.Country;
import com.flightlogger.backend.model.CountryReadDto;

import java.util.UUID;

public class CountryTestData {

    public static final Country CANADA_COUNTRY = new Country(
            UUID.fromString("f85e30a9-8083-46a4-b743-bb74bf787c9c"),
            "Canada",
            "CA",
            "CAN",
            "🇨🇦"
    );

    public static final CountryReadDto CANADA_READ_DTO = new CountryReadDto()
            .id(CANADA_COUNTRY.getId())
            .name(CANADA_COUNTRY.getName())
            .isoCode2(CANADA_COUNTRY.getIsoCode2())
            .isoCode3(CANADA_COUNTRY.getIsoCode3())
            .flagEmoji(CANADA_COUNTRY.getFlagEmoji());
}
