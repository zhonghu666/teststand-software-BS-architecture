package com.cetiti.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Date;

@WritingConverter
public class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
    @Override
    public Date convert(LocalDateTime source) {
        return Date.from(source.atZone(ZoneId.systemDefault()).toInstant());
    }
}

