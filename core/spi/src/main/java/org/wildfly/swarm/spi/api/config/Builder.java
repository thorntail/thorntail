package org.wildfly.swarm.spi.api.config;

import java.net.MalformedURLException;
import java.net.URL;

/**
 */
public class Builder<T> implements Resolver<T> {

    public Builder(ConfigView view, ConfigKey key) {
        this.view = view;
        this.key = key;
    }

    public <N> Resolver<N> as(Class<N> clazz) {
        targetType = clazz;
        return (Resolver<N>) this;
    }

    @Override
    public <N> Resolver<N> as(Class<N> clazz, Converter<N> converter) {
        targetType = clazz;
        this.converter = converter;
        return (Resolver<N>) this;
    }

    @Override
    public ConfigKey getKey() {
        return key;
    }

    @Override
    public T getValue() {

        Object originalValue = this.view.valueOf(this.key);
        String valueStr = (originalValue != null ? originalValue.toString() : null);

        try {
            T value = convert(valueStr);
            if (null == value) {
                throw new RuntimeException("Stage config '" + key + "' is missing");
            }
            return value;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public boolean hasValue() {
        Object originalValue = this.view.valueOf(this.key);
        return originalValue != null;
    }

    public Resolver<T> withDefault(T value) {
        defaultValue = value;
        return this;
    }

    private T convert(String value) throws MalformedURLException {

        if (value == null) {
            if (defaultValue != null) {
                if (defaultValue instanceof String) {
                    value = (String) defaultValue;
                } else {
                    return defaultValue;
                }
            } else {
                return null;
            }
        }

        Object result = null;

        if (this.converter != null) {
            try {
                result = converter.convert(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (String.class.equals(targetType)) {
            result = value;
        } else if (Boolean.class.equals(targetType)) {
            Boolean isTrue = "TRUE".equalsIgnoreCase(value);
            isTrue |= "1".equalsIgnoreCase(value);

            result = isTrue;
        } else if (Integer.class.equals(targetType)) {
            result = Integer.parseInt(value);
        } else if (Long.class.equals(targetType)) {
            result = Long.parseLong(value);
        } else if (Float.class.equals(targetType)) {
            result = Float.parseFloat(value);
        } else if (Double.class.equals(targetType)) {
            result = Double.parseDouble(value);
        } else if (URL.class.equals(targetType)) {
            result = new URL(value);
        }

        return (T) result;
    }

    private ConfigView view;

    private final ConfigKey key;

    private Class<?> targetType;

    private T defaultValue;

    private Converter<?> converter;
}
