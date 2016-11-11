/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.spi.api;

import java.util.Set;

/**
 * @author Heiko Braun
 * @since 19/04/16
 */
public class StageConfig {


    public StageConfig(ProjectStage stage) {
        this.stage = stage;
    }

    public Resolver<String> resolve(String name)
    {
        return new Builder<String>(name).as(String.class);
    }

    public Set<String> keys() {
        return this.stage.getProperties().keySet();
    }

    public String getName() {
        return this.stage.getName();
    }

    private final ProjectStage stage;

    public interface Resolver<T> {
        T getValue();
        boolean hasValue();
        Resolver<T> withDefault(T value);
        String getKey();
        <N> Resolver<N> as(Class<N> clazz);
        <N> Resolver<N> as(Class<N> clazz, Converter<T> converter);
    }

    public interface Converter<T> {
        T convert(String val);
    }

    public class Builder<T> implements Resolver<T> {

        public Builder(String key) {
            this.key = key;
        }

        public <N> Resolver<N> as(Class<N> clazz)
        {
            targetType = clazz;
            return (Resolver<N>) this;
        }

        @Override
        public <N> Resolver<N> as(Class<N> clazz, Converter<T> converter) {
            targetType = clazz;
            this.converter = converter;
            return (Resolver<N>) this;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public T getValue() {

            String valueStr = stage.getProperties().get(key);
            if ( valueStr == null ) {
                valueStr = System.getProperty(key);
            }
            T value = convert(valueStr);

            if(null==value) {
                throw new RuntimeException("Stage config '" + key + "' is missing");
            }

            return value;
        }

        @Override
        public boolean hasValue() {
            String valueStr = stage.getProperties().get(key);
            if ( valueStr == null ) {
                valueStr = System.getProperty(key);
            }

            return valueStr != null;
        }

        public Resolver<T> withDefault(T value)
        {
            defaultValue = value;
            return this;
        }

        private T convert(String value)
        {

            if (value == null)
            {
                if ( defaultValue != null ) {
                    if ( defaultValue instanceof String ) {
                        value = (String) defaultValue;
                    } else {
                        return defaultValue;
                    }
                } else {
                    return null;
                }
            }

            Object result = null;

            if (this.converter != null)
            {
                try
                {
                    result = converter.convert(value);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            else if (String.class.equals(targetType))
            {
                result = value;
            }
            else if (Boolean.class.equals(targetType))
            {
                Boolean isTrue = "TRUE".equalsIgnoreCase(value);
                isTrue |= "1".equalsIgnoreCase(value);

                result = isTrue;
            }
            else if (Integer.class.equals(targetType))
            {
                result = Integer.parseInt(value);
            }
            else if (Long.class.equals(targetType))
            {
                result = Long.parseLong(value);
            }
            else if (Float.class.equals(targetType))
            {
                result = Float.parseFloat(value);
            }
            else if (Double.class.equals(targetType))
            {
                result = Double.parseDouble(value);
            }

            return (T) result;
        }

        private final String key;

        private Class<?> targetType;

        private T defaultValue;

        private Converter<T> converter;
    }
}
