package org.jboss.unimbus.config.impl.converters.fallback;

import org.jboss.unimbus.config.impl.converters.FallbackConverter;

public class EnumValueOfConverter implements FallbackConverter {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(String value, Class<T> type) {
        if (type.isEnum()) {
            Class<? extends Enum> enumType = (Class<? extends Enum>) type;
            return (T) type.cast(Enum.valueOf(enumType, value));
        }
        return null;
    }

}
