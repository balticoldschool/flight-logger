package com.flightlogger.backend.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@Named("StringFormatter")
public class StringFormatter {

    @Named("toUpperCase")
    public String toUpperCase(String value) {
        return value != null ? StringUtils.upperCase(value) : null;
    }
}
