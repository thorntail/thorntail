package io.thorntail.agroal.impl.converter;

import org.eclipse.microprofile.config.spi.Converter;

import java.time.Duration;

public class DurationConverter implements Converter<Duration> {
    @Override
    public Duration convert(String value) {
        return value != null ? Duration.parse(value) : null;
    }
}
