package io.thorntail.config.impl.converters;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.microprofile.config.spi.Converter;

public class URLConverter implements Converter<URL> {

    @Override
    public URL convert(String value) {
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
