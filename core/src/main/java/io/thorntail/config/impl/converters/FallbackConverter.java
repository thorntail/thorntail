package io.thorntail.config.impl.converters;

public interface FallbackConverter {

    <T> T convert(String value, Class<T> type);

}
