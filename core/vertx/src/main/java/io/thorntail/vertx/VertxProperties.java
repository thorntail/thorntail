package io.thorntail.vertx;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;

public final class VertxProperties {

    public static final String PROPERTY_PREFIX = "vertx";

    private VertxProperties() {
    }

    public static <T> T createOptions(Class<T> optionsClazz, Config config, String prefix) throws InstantiationException, IllegalAccessException {
        T options = optionsClazz.newInstance();
        List<Method> setters = new ArrayList<>();
        for (Method method : optionsClazz.getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                setters.add(method);
            }
        }
        for (Method setter : setters) {
            Optional<?> value = config.getOptionalValue(getPropertyName(setter.getName(), prefix), setter.getParameterTypes()[0]);
            value.ifPresent(v -> {
                try {
                    setter.invoke(options, v);
                } catch (Exception e) {
                    VertxLogger.LOG.warnf("Unable to set %s using %s", v, setter);
                }
            });
        }
        return options;
    }

    public static String getPropertyName(String setterName, String prefix) {
        StringBuilder name = new StringBuilder(prefix);
        for (String part : VertxProperties.splitByCamelCase(setterName.substring(3))) {
            name.append(".");
            name.append(part.toLowerCase());
        }
        return name.toString();
    }

    public static String[] splitByCamelCase(String value) {
        Objects.requireNonNull(value);
        char[] chars = value.toCharArray();
        List<String> parts = new ArrayList<>();

        int currentType = Character.getType(chars[0]);
        int start = 0;

        for (int i = 1; i < chars.length; i++) {
            int type = Character.getType(chars[i]);
            if (type == currentType) {
                continue;
            }
            if (type == Character.UPPERCASE_LETTER) {
                parts.add(value.substring(start, i));
                start = i;
            }
            currentType = type;
        }
        parts.add(value.substring(start));
        return parts.toArray(new String[parts.size()]);
    }

}
