package org.jboss.unimbus.config.mp.converters;

import org.eclipse.microprofile.config.spi.Converter;

public class IntegerConverter implements Converter<Integer> {

    @Override
    public Integer convert(String value) {
        return Integer.parseInt(value);
    }
}
