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
