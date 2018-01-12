package org.jboss.unimbus.config.mp.converters;

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

        if (value.equals("YES") || value.equals("Y")) {
            return true;
        }

        if (value.equals("ON")) {
            return true;
        }

        return false;
    }
}
