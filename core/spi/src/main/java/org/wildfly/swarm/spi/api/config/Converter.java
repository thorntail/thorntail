package org.wildfly.swarm.spi.api.config;

/**
 * Converter capable of converting a native {@code String} value to a specific type.
 *
 * @param <T> The type to coerce to.
 */
public interface Converter<T> {
    T convert(String val);
}
