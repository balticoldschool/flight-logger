package utils;

import com.flightlogger.backend.model.CountryCreateDto;

import java.util.function.Consumer;

@FunctionalInterface
public interface CountryMutator extends Consumer<CountryCreateDto> {
}
