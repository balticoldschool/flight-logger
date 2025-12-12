package com.flightlogger.backend.domain.airline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("JpaDataSourceORMInspection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "airline")
public class Airline {
    @Id
    @Column(name = "icao_code", length = 3, nullable = false)
    private String icaoCode;

    @Column(name = "iata_code", length = 2, nullable = false)
    private String iataCode;

    @Column(name = "name", length = 127, nullable = false)
    private String name;

    @Column(name = "image_link")
    private String imageLink;
}
