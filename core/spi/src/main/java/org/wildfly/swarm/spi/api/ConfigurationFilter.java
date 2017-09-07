package org.wildfly.swarm.spi.api;

/**
 * SPI for user-provided configuration value filtering.
 *
 * <p>Implementations will be discovered within the user's classpath
 * and given an opportunity to filter any user-provided configuration
 * values.</p>
 *
 * Created by bob on 8/31/17.
 */
public interface ConfigurationFilter {

    /**
     * Filter the provided configuration key and value.
     *
     * <p>Each filter will be given an opportunity to filter
     * each user-provided value. If the filter chooses to
     * perform no filtering, it should simply return the
     * original value.</p>
     *
     * @param key   The key of the configuration item to filter.
     * @param value The value to filter.
     * @param <T>   The type of the filtered value.
     * @return The filtered value.
     */
    <T> T filter(String key, T value);
}
