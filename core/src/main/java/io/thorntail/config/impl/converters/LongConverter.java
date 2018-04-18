package io.thorntail.config.impl.converters;

import org.eclipse.microprofile.config.spi.Converter;

public class LongConverter implements Converter<Long> {

    @Override
    public Long convert(String value) {
        return Long.parseLong(value);
    }
}
