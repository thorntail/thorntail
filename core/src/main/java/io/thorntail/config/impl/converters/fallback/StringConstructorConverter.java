package io.thorntail.config.impl.converters.fallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.thorntail.config.impl.converters.FallbackConverter;

public class StringConstructorConverter implements FallbackConverter {

    @Override
    public <T> T convert(String value, Class<T> type) {
        try {
            Constructor<T> ctor = findConstructor(type);
            if ( ctor == null ) {
                return null;
            }
            return ctor.newInstance(value);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // ignore
        }
        return null;
    }

    private <T> Constructor<T> findConstructor(Class<T> type) throws NoSuchMethodException {
        Constructor<?>[] ctors = type.getDeclaredConstructors();
        for ( int i = 0 ; i < ctors.length ; ++i) {
            if ( ctors[i].getParameterCount() == 1 ) {
                if ( ctors[i].getParameterTypes()[0].isAssignableFrom(String.class)) {
                    ctors[i].setAccessible(true);
                    return (Constructor<T>) ctors[i];
                }
            }
        }

        return null;
    }

}
