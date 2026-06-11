package com.flightlogger.backend.domain.flight.controller;

import com.flightlogger.backend.api.FlightsApi;
import com.flightlogger.backend.common.utils.PaginationUtils;
import com.flightlogger.backend.domain.flight.service.FlightService;
import com.flightlogger.backend.model.FlightReadDto;
import com.flightlogger.backend.model.PagedFlightReadResponse;
import com.flightlogger.backend.model.PaginationMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class FlightController implements FlightsApi {

    private final FlightService flightService;

    @Override
    public ResponseEntity<PagedFlightReadResponse> getAllFlights(Integer page, Integer pageSize) {
        Page<FlightReadDto> pagedFlightRead = flightService.getAllFlights(page, pageSize);
        PaginationMetadata metadata = PaginationUtils.toMetaData(pagedFlightRead);

        return ResponseEntity.ok(new PagedFlightReadResponse(pagedFlightRead.getContent(), metadata));
    }
}
