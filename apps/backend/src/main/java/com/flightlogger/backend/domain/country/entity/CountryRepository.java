package com.flightlogger.backend.domain.country.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {

    @Query("""
        SELECT c FROM Country c
            WHERE lower(c.name) LIKE lower(concat('%', :query, '%'))
                OR lower(c.isoCode2) LIKE lower(concat('%', :query, '%'))
                OR lower(c.isoCode3) LIKE lower(concat('%', :query, '%'))
                OR c.flagEmoji =  :query
            ORDER BY
                CASE
                    WHEN lower(c.isoCode2) = lower(:query) THEN 1
                    WHEN lower(c.isoCode3) = lower(:query) THEN 2
                    WHEN lower(c.name) = lower(:query) THEN 3
                    WHEN lower(c.name) LIKE lower(concat(:query, '%')) THEN 4
                ELSE 5
                END ASC,
                    c.name  ASC
   \s""")
    Page<Country> searchWithString(@Param("query") String searchTerm, Pageable pageable);

    boolean existsByIsoCode2(String iso2Code);

    boolean existsByIsoCode3(String iso3Code);
}
