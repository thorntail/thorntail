package org.jboss.unimbus.config.impl.converters;

public interface FallbackConverter {

    <T> T convert(String value, Class<T> type);

}
