package io.thorntail.config.impl.converters;

import org.eclipse.microprofile.config.spi.Converter;

public class IntegerConverter implements Converter<Integer> {

    @Override
    public Integer convert(String value) {
        return Integer.parseInt(value);
    }
}
