package org.jboss.unimbus.config.mp.converters;

import org.eclipse.microprofile.config.spi.Converter;

public class ClassConverter implements Converter<Class> {

    public ClassConverter(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Class convert(String value) {
        try {
            return Class.forName(value, true, this.classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private final ClassLoader classLoader;
}
