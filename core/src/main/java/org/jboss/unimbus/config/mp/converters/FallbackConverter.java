package org.jboss.unimbus.config.mp.converters;

public interface FallbackConverter {

    <T> T convert(String value, Class<T> type);

}
