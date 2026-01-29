package com.flightlogger.backend.common.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class StringFormatterTest {

    private final StringFormatter stringFormatter = new StringFormatter();

    @Test
    void toUpperCase_shouldConvertTextToUpperCase() {
        // When
        String result = stringFormatter.toUpperCase("hello world");

        // Then
        assertThat(result).isEqualTo("HELLO WORLD");
    }

    @Test
    void toUpperCase_shouldReturnNull_whenInputIsNull() {
        // When
        String result = stringFormatter.toUpperCase(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toUpperCase_shouldReturnEmpty_whenInputIsEmpty() {
        // When
        String result = stringFormatter.toUpperCase("");

        // Then
        assertThat(result).isEmpty();
    }
}