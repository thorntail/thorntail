package io.thorntail.config.impl.converters;

import org.eclipse.microprofile.config.spi.Converter;

public class BooleanConverter implements Converter<Boolean> {

    @Override
    public Boolean convert(String value) {
        if (value.toLowerCase().equals("true")) {
            return true;
        }
        if (value.equals("1")) {
            return true;
        }

        if (value.equalsIgnoreCase("YES") || value.equalsIgnoreCase("Y")) {
            return true;
        }

        if (value.equalsIgnoreCase("ON")) {
            return true;
        }

        if (value.equalsIgnoreCase("OFF")) {
            return false;
        }

        return false;
    }
}
