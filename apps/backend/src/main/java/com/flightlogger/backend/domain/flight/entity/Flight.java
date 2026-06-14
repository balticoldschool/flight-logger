package com.flightlogger.backend.domain.flight.entity;

import com.flightlogger.backend.domain.airline.entity.Airline;
import com.flightlogger.backend.domain.airport.entity.Airport;
import com.flightlogger.backend.model.BookingClass;
import com.flightlogger.backend.model.TravelPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "flight")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "flight_date", nullable = false)
    private LocalDateTime flightDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_icao", nullable = false)
    private Airport origin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_icao", nullable = false)
    private Airport destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_icao", nullable = false)
    private Airline airline;

    @Column(name = "flight_number", length = 8, nullable = false)
    private String flightNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_class", length = 32, nullable = false)
    private BookingClass bookingClass;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationInMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 32, nullable = false)
    private TravelPurpose purpose;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime updatedAt;

}
