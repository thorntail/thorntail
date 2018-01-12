package org.jboss.unimbus.config.mp.converters.fallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jboss.unimbus.config.mp.converters.FallbackConverter;

public class StringConstructorConverter implements FallbackConverter {

    @Override
    public <T> T convert(String value, Class<T> type) {
        try {
            Constructor<T> ctor = findConstructor(type);
            return ctor.newInstance(value);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // ignore
        }
        return null;
    }

    private <T> Constructor<T> findConstructor(Class<T> type) throws NoSuchMethodException {
        return type.getConstructor(String.class);
    }

}
