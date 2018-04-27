package io.thorntail.config.impl.converters;

import org.eclipse.microprofile.config.spi.Converter;

public class DoubleConverter implements Converter<Double> {

    @Override
    public Double convert(String value) {
        return Double.parseDouble(value);
    }
}
