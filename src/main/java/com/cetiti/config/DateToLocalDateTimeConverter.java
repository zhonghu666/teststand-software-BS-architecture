package com.cetiti.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
    @Override
    public LocalDateTime convert(Date source) {
        return source.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}

