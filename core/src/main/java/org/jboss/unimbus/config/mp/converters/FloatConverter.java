package org.jboss.unimbus.config.mp.converters;

import org.eclipse.microprofile.config.spi.Converter;

public class FloatConverter implements Converter<Float> {

    @Override
    public Float convert(String value) {
        return Float.parseFloat(value);
    }
}
