package com.flightlogger.backend.domain.airline.service;

import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.domain.airline.entity.AirlineMapper;
import com.flightlogger.backend.domain.airline.entity.AirlineRepository;
import com.flightlogger.backend.domain.airline.exception.AirlineAlreadyExistsException;
import com.flightlogger.backend.domain.airline.exception.AirlineNotFoundException;
import com.flightlogger.backend.model.AirlineCreateDto;
import com.flightlogger.backend.model.AirlineReadDto;
import com.flightlogger.backend.model.AirlineUpdateDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository airlineRepository;
    private final AirlineMapper airlineMapper;

    public List<Airline> getAllAirlines(){
        return airlineRepository.findAll();
    }

    @Override
    public Airline getAirlineByIcao(String airlineIcao) {
        return airlineRepository.findById(airlineIcao).orElseThrow(() -> new AirlineNotFoundException(airlineIcao));
    }

    @Override
    @Transactional
    public AirlineReadDto saveAirline(AirlineCreateDto dto) {
        String icaoCode = StringUtils.upperCase(dto.getIcao());
        String iataCode = StringUtils.upperCase(dto.getIata());

        if (airlineRepository.existsByIcaoCode(icaoCode)) {
            throw new AirlineAlreadyExistsException("ICAO", icaoCode);
        }

        if (airlineRepository.existsByIataCode(iataCode)) {
            throw new AirlineAlreadyExistsException("IATA", iataCode);
        }

        // Save entity
        Airline newAirline = airlineMapper.toEntity(dto);
        Airline savedAirline = airlineRepository.save(newAirline);

        // Map back to ReadDto
        return airlineMapper.toDto(savedAirline);
    }

    @Override
    @Transactional
    public AirlineReadDto updateAirline(String icaoCode, AirlineUpdateDto dto) {
        String iataCode = StringUtils.upperCase(dto.getIata());
        String icaocode = StringUtils.upperCase(icaoCode);

        Airline airline = airlineRepository
                .findById(icaocode)
                .orElseThrow(() -> new AirlineNotFoundException(icaocode));

        if (!iataCode.equals(airline.getIataCode()) && airlineRepository.existsByIataCode(iataCode)) {
            throw new AirlineAlreadyExistsException("IATA", iataCode);
        }

        airlineMapper.updateFromDto(dto, airline);

        return airlineMapper.toDto(airline);
    }

    @Override
    @Transactional
    public void deleteAirline(String airlineIcao) {
        airlineRepository.deleteById(StringUtils.upperCase(airlineIcao));
    }
}
