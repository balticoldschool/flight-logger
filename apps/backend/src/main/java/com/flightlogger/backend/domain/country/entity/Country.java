package com.flightlogger.backend.domain.country.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "country")
public class Country {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 127)
    private String name;

    @Column(name = "iso_code_2", nullable = false, length = 2, unique = true)
    private String isoCode2;

    @Column(name = "iso_code_3", nullable = false, length = 3, unique = true)
    private String isoCode3;

    @Column(name = "flag_emoji", length = 127)
    private String flagEmoji;
}
