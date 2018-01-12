package org.jboss.unimbus.config.mp.converters;

import org.eclipse.microprofile.config.spi.Converter;

public class LongConverter implements Converter<Long> {

    @Override
    public Long convert(String value) {
        return Long.parseLong(value);
    }
}
