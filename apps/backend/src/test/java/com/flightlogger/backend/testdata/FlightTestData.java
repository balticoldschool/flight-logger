package com.flightlogger.backend.testdata;

import com.flightlogger.backend.model.BookingClass;
import com.flightlogger.backend.model.FlightReadDto;
import com.flightlogger.backend.model.TravelPurpose;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.flightlogger.backend.testdata.AirlineTestData.DLH_READ_DTO;
import static com.flightlogger.backend.testdata.AirportTestData.FRANKFURT_AIRPORT_READ_DTO;
import static com.flightlogger.backend.testdata.AirportTestData.MUNICH_AIRPORT_READ_DTO;

public class FlightTestData {

    public static final FlightReadDto FRA_MUC_FLIGHT_READ_DTO = new FlightReadDto()
            .id(UUID.fromString("a1b2c3d4-0001-4000-8000-000000000001"))
            .flightDate(LocalDateTime.parse("2026-06-11T10:30:00"))
            .origin(FRANKFURT_AIRPORT_READ_DTO)
            .destination(MUNICH_AIRPORT_READ_DTO)
            .airline(DLH_READ_DTO)
            .flightNumber("LH100")
            .bookingClass(BookingClass.ECONOMY)
            .durationInMinutes(55)
            .purpose(TravelPurpose.BUSINESS)
            .createdAt(LocalDateTime.parse("2026-06-11T10:30:00"))
            .updatedAt(LocalDateTime.parse("2026-06-11T10:30:00"));
}
