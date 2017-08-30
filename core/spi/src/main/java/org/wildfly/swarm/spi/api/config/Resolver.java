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

/**
 * Coercing resolver for a given configuration item.
 *
 * @param <T> The coercion type.
 */
public interface Resolver<T> {
    /**
     * Retrieve the coerced value.
     *
     * @return The coerced value.
     */
    T getValue();

    /**
     * Determine if there is any value set.
     *
     * @return {@code true} if a value is set, otherwise {@code false}
     */
    boolean hasValue();

    /**
     * Provide a default value to be provided in the case no value is currently bound.
     *
     * @param value The default fall-back value.
     * @return This resolver.
     */
    Resolver<T> withDefault(T value);

    /**
     * Retrieve the key of the configuration item.
     *
     * @return The key.
     */
    ConfigKey getKey();

    /**
     * Retrieve a resolver capable of coercing to another simple type.
     *
     * @param clazz The class to coerce to.
     * @param <N>   The value type to coerce to.
     * @return A new resolver capable of coercing to the given type.
     */
    <N> Resolver<N> as(Class<N> clazz);

    /**
     * Retrieve a resolver capable of coering to another type.
     *
     * @param clazz     The class to coerce to.
     * @param converter The converter to support the coercion.
     * @param <N>       The value type to coerce to.
     * @return A new resolver capable of coercing to the given type.
     */
    <N> Resolver<N> as(Class<N> clazz, Converter<N> converter);
}
