package com.flightlogger.backend.domain.flight.service;

import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.domain.airline.service.AirlineService;
import com.flightlogger.backend.domain.airport.entity.Airport;
import com.flightlogger.backend.domain.airport.service.AirportService;
import com.flightlogger.backend.domain.flight.entity.Flight;
import com.flightlogger.backend.domain.flight.entity.FlightMapper;
import com.flightlogger.backend.domain.flight.entity.FlightRepository;
import com.flightlogger.backend.domain.flight.exception.DuplicateFlightNumberException;
import com.flightlogger.backend.domain.flight.exception.FlightNotFoundException;
import com.flightlogger.backend.domain.flight.exception.OverlappingFlightException;
import com.flightlogger.backend.model.FlightCreateDto;
import com.flightlogger.backend.model.FlightReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private static final int MAX_FLIGHT_DURATION_MINUTES = 1440;

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final AirportService airportService;
    private final AirlineService airlineService;

    @Override
    public Page<FlightReadDto> getAllFlights(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("flightDate").ascending());

        return flightRepository.findAll(pageable).map(flightMapper::toDto);
    }

    @Override
    public FlightReadDto getFlightById(UUID flightId) {
        return flightRepository.findById(flightId)
                .map(flightMapper::toDto)
                .orElseThrow(FlightNotFoundException::new);
    }

    @Override
    @Transactional
    public FlightReadDto saveFlight(FlightCreateDto dto) {
        Airport origin = airportService.getAirportEntityByIcao(dto.getOriginIcao());
        Airport destination = airportService.getAirportEntityByIcao(dto.getDestinationIcao());
        Airline airline = airlineService.getAirlineEntityByIcao(dto.getAirlineIcao());

        validateNoTimeOverlap(dto);
        validateFlightNumberUniqueOnDate(dto);

        Flight newFlight = flightMapper.toEntity(dto, origin, destination, airline);

        return flightMapper.toDto(flightRepository.save(newFlight));
    }

    private void validateNoTimeOverlap(FlightCreateDto dto) {
        LocalDateTime newStart = dto.getFlightDate();
        LocalDateTime newEnd = newStart.plusMinutes(dto.getDurationInMinutes());
        LocalDateTime candidateWindowStart = newStart.minusMinutes(MAX_FLIGHT_DURATION_MINUTES);

        boolean hasOverlap = flightRepository.findByFlightDateBetween(candidateWindowStart, newEnd)
                .stream()
                .anyMatch(existing -> overlaps(existing, newStart, newEnd));

        if (hasOverlap) {
            throw new OverlappingFlightException();
        }
    }

    private boolean overlaps(Flight existing, LocalDateTime newStart, LocalDateTime newEnd) {
        LocalDateTime existingStart = existing.getFlightDate();
        LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationInMinutes());
        return existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart);
    }

    private void validateFlightNumberUniqueOnDate(FlightCreateDto dto) {
        LocalDateTime dayStart = dto.getFlightDate().toLocalDate().atStartOfDay();
        LocalDateTime nextDayStart = dayStart.plusDays(1);

        boolean exists = flightRepository
                .existsByAirline_IcaoCodeAndFlightNumberAndFlightDateGreaterThanEqualAndFlightDateLessThan(
                        dto.getAirlineIcao(), dto.getFlightNumber(), dayStart, nextDayStart);

        if (exists) {
            throw new DuplicateFlightNumberException();
        }
    }
}