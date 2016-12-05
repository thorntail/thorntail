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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Handle to stage-centric configuration items.
 *
 * <p>A view into {@link ProjectStage} for an active set of configuration items.</p>
 *
 * @author Heiko Braun
 * @since 19/04/16
 */
public class StageConfig {

    public StageConfig(ProjectStage stage) {
        this.stage = stage;
    }

    /** Obtain a resolver for a given configuration item name.
     *
     * <p>While the default resolver is {@code String}-based, the resolver
     * can be used to obtain the configuration value as a different type.</p>
     *
     * @param name The configuration item name.
     * @return The resolver for the configuration item.
     */
    public Resolver<String> resolve(String name)
    {
        return new Builder<String>(name).as(String.class);
    }

    /** Retrieve the set of explicit keys in the stage.
     *
     * @return The set of keys.
     */
    public Set<String> keys() {
        return this.stage.getProperties().keySet();
    }

    /** Retrieve the set of sub-keys that are immediately underneath the specified prefix.
     *
     * <p>If, for example, a prefix of {@code swarm.foo.bar} is provided as the parameter,
     * and there exists the following complete keys:</p>
     *
     * <ul>
     *     <li>{@code swarm.foo.bar.x}</li>
     *     <li>{@code swarm.foo.bar.y}</li>
     *     <li>{@code swarm.foo.bar.z}</li>
     *     <li>{@code swarm.foo.barbara}</li>
     * </ul>
     *
     * <p>Then this method would return simply the set of {@code [x, y, z]}</p>
     *
     * @param prefix The prefix to search for.
     * @return
     */
    public Set<String> simpleSubkeys(String prefix) {

        String searchPrefix = prefix + ".";

        Set<String> allKeys = new HashSet<>();
        for (Object o : System.getProperties().keySet()) {
            allKeys.add( o.toString() );
        }

        allKeys.addAll( keys() );

        return allKeys
                .stream()
                .filter( e-> e.startsWith(searchPrefix) )
                .map( e->e.replace( searchPrefix, "" ) )
                .map( e->{
                    int dotLoc = e.indexOf('.');
                    if ( dotLoc < 0 ) {
                        return e;
                    }
                    return e.substring( 0, dotLoc );
                })
                .collect(Collectors.toSet());
    }


    /** Determine if the specified key or any sub-keys exists.
     *
     * @param key The key to test.
     * @return {@code true} if the key or any sub-key exists, otherwise {@code false}.
     */
    public boolean hasKeyOrSubkeys(String key) {
        String searchPrefix = key + ".";

        Set<String> allKeys = new HashSet<>();
        for (Object o : System.getProperties().keySet()) {
            allKeys.add( o.toString() );
        }

        allKeys.addAll( keys() );

        return allKeys
                .stream()
                .anyMatch( e-> e.equals(key) || e.startsWith( searchPrefix ));
    }

    /** Retrieve the name of this stage.
     *
     * @return The name of this stage.
     */
    public String getName() {
        return this.stage.getName();
    }

    private final ProjectStage stage;

    /** Coercing resolver for a given configuration item.
     *
     * @param <T> The coercion type.
     */
    public interface Resolver<T> {
        /** Retrieve the coerced value.
         *
         * @return The coerced value.
         */
        T getValue();

        /** Determine if there is any value set.
         *
         * @return {@code true} if a value is set, otherwise {@code false}
         */
        boolean hasValue();

        /** Provide a default value to be provided in the case no value is currently bound.
         *
         * @param value The default fall-back value.
         * @return This resolver.
         */
        Resolver<T> withDefault(T value);

        /** Retrieve the key of the configuration item.
         *
         * @return The key.
         */
        String getKey();

        /** Retrieve a resolver capable of coercing to another simple type.
         *
         * @param clazz The class to coerce to.
         * @param <N> The value type to coerce to.
         * @return A new resolver capable of coercing to the given type.
         */
        <N> Resolver<N> as(Class<N> clazz);

        /** Retrieve a resolver capable of coering to another type.
         *
         * @param clazz The class to coerce to.
         * @param converter The converter to support the coercion.
         * @param <N> The value type to coerce to.
         * @return A new resolver capable of coercing to the given type.
         */
        <N> Resolver<N> as(Class<N> clazz, Converter<T> converter);
    }

    /** Converter capable of converting a native {@code String} value to a specific type.
     *
     * @param <T> The type to coerce to.
     */
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
            T value = null;
            try {
                value = convert(valueStr);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

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

        private T convert(String value) throws MalformedURLException {

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
            else if ( URL.class.equals(targetType)) {
                result = new URL( value );
            }

            return (T) result;
        }

        private final String key;

        private Class<?> targetType;

        private T defaultValue;

        private Converter<T> converter;
    }
}
