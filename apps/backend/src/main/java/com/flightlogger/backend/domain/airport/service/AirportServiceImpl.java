package com.flightlogger.backend.domain.airport.service;

import com.flightlogger.backend.common.utils.IdentificationCodeTypes;
import com.flightlogger.backend.domain.airport.entity.Airport;
import com.flightlogger.backend.domain.airport.entity.AirportMapper;
import com.flightlogger.backend.domain.airport.entity.AirportRepository;
import com.flightlogger.backend.domain.airport.exception.AirportAlreadyExistsException;
import com.flightlogger.backend.domain.airport.exception.AirportNotFoundException;
import com.flightlogger.backend.domain.country.service.CountryService;
import com.flightlogger.backend.model.AirportCreateDto;
import com.flightlogger.backend.model.AirportReadDto;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AirportServiceImpl implements AirportService {

    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;
    private final CountryService countryService;

    @Override
    @Transactional(readOnly = true)
    public List<AirportReadDto> getAllAirports() {
        List<Airport> airports = airportRepository.findAll();
        return airports.stream().map(airportMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AirportReadDto getAirportByIcao(String airportIcao) {
        return airportRepository.findById(airportIcao)
                .map(airportMapper::toDto)
                .orElseThrow(() -> new AirportNotFoundException(airportIcao));
    }

    @Override
    @Transactional
    public AirportReadDto saveAirport(AirportCreateDto dto) {
        final String icaoCode = StringUtils.upperCase(dto.getIcao());
        final String iataCode = StringUtils.upperCase(dto.getIata());

        if (airportRepository.existsById(icaoCode)) {
            throw new AirportAlreadyExistsException(IdentificationCodeTypes.ICAO, icaoCode);
        }

        if (airportRepository.existsByIataCode(iataCode)) {
            throw new AirportAlreadyExistsException(IdentificationCodeTypes.IATA, iataCode);
        }

        Airport savedAirport = airportRepository.save(
                airportMapper.toEntity(dto, countryService.getCountryEntityById(dto.getCountryId()))
        );

        return airportMapper.toDto(savedAirport);
    }
}
