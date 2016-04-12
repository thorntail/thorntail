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
            T value = convert(valueStr);

            if(null==value)
                throw new RuntimeException("Stage config '"+key+"' is missing");

            return value;
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
                return defaultValue!=null ? defaultValue : null;
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
