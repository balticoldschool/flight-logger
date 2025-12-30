package com.flightlogger.backend.common.utils;

import com.flightlogger.backend.model.PaginationMetadata;
import org.springframework.data.domain.Page;

public class PaginationUtils {

    private PaginationUtils() {
        throw new UnsupportedOperationException("PaginationUtils is a utility class and cannot be instantiated");
    }

    public static PaginationMetadata toMetaData(Page<?> page) {
        return new PaginationMetadata(page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }
}
