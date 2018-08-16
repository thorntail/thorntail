/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.spi.api.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 */
public class Builder<T> implements Resolver<T> {

    public Builder(ConfigView view, ConfigKey key) {
        this.view = view;
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    public <N> Resolver<N> as(Class<N> clazz) {
        targetType = clazz;
        return (Resolver<N>) this;
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    @Override
    public T getValue() {

        Object originalValue = this.view.valueOf(this.key);

        if (originalValue instanceof ConfigTree) {
            if (List.class.isAssignableFrom(this.targetType)) {
                return (T) ((ConfigTree) originalValue).asList();
            } else if (Properties.class.isAssignableFrom(this.targetType)) {
                return (T) ((ConfigTree) originalValue).asProperties();
            } else if (Map.class.isAssignableFrom(this.targetType)) {
                return (T) ((ConfigTree) originalValue).asMap();
            } else {
                return null;
            }
        } else {

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

    @SuppressWarnings("unchecked")
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
