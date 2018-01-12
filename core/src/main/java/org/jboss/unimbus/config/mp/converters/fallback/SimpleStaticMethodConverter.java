package org.jboss.unimbus.config.mp.converters.fallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jboss.unimbus.config.mp.converters.FallbackConverter;

public class SimpleStaticMethodConverter implements FallbackConverter {

    protected SimpleStaticMethodConverter(String methodName, Class<?> paramterType) {
        this.methodName = methodName;
        this.parameterType = paramterType;
    }

    @Override
    public <T> T convert(String value, Class<T> type) {
        Method method = findMethod(type);
        if ( method == null ) {
            return null;
        }
        try {
            return type.cast(method.invoke(null, value));
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignore
        }
        return null;
    }

    private <T> Method findMethod(Class<T> type) {
        Method[] methods = type.getMethods();
        for (Method each : methods) {
            if ( ! each.getName().equals(this.methodName) ) {
                continue;
            }
            Class<?>[] params = each.getParameterTypes();
            if ( params.length != 1 ) {
                continue;
            }
            if ( ! this.parameterType.isAssignableFrom(params[0])) {
                continue;
            }
            int modifiers = each.getModifiers();
            if (! Modifier.isPublic(modifiers) || ! Modifier.isStatic(modifiers)) {
                continue;
            }
            if ( type.isAssignableFrom(each.getReturnType()) ) {
                return each;
            }
        }

        return null;
    }

    private final String methodName;
    private final Class<?> parameterType;
}
