package com.flightlogger.backend.domain.airport.entity;

import com.flightlogger.backend.domain.country.entity.Country;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "airport")
public class Airport {

    @Id
    @Column(name = "icao_code", length = 4)
    private String icaoCode;

    @Column(name = "iata_code", length = 3, unique = true, nullable = false)
    private String iataCode;

    @Column(name = "name", length = 127, nullable = false)
    private String name;

    @Column(name = "city", length = 127, nullable = false)
    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "timezone", nullable = false)
    private String timezone;
}
