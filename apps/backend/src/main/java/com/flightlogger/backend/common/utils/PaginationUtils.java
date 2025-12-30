package com.flightlogger.backend.common.utils;

import com.flightlogger.backend.model.PaginationMetadata;
import org.springframework.data.domain.Page;

public class PaginationUtils {

    public static PaginationMetadata toMetaData(Page<?> page) {
        return new PaginationMetadata(page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }
}
