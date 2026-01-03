package com.flightlogger.backend.common.utils;

import com.flightlogger.backend.model.AirlineCreateDto;

import java.util.function.Consumer;

@FunctionalInterface
public interface AirlineMutator extends Consumer<AirlineCreateDto> {
}
