package org.wildfly.swarm.container.runtime;

import java.lang.reflect.Field;

import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.config.ConfigKey;

/**
 * @author Bob McWhirter
 */
public class ObjectBackedConfigurableHandle implements ConfigurableHandle {

    private final ConfigKey key;

    private final Object instance;

    private final Field field;

    public ObjectBackedConfigurableHandle(ConfigKey key, Object instance, Field field) {
        this.key = key;
        this.instance = instance;
        this.field = field;
        this.field.setAccessible(true);
    }

    @Override
    public ConfigKey key() {
        return this.key;
    }

    @Override
    public Class<?> type() throws IllegalAccessException {
        if (isDefaultable()) {
            return ((Defaultable) this.field.get(this.instance)).type();
        }

        return this.field.getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void set(T value) throws IllegalAccessException {
        if (isDefaultable()) {
            ((Defaultable<T>) this.field.get(this.instance)).set(value);
        } else {
            this.field.set(this.instance, value);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T currentValue() throws IllegalAccessException {
        Object value = this.field.get(this.instance);
        if (value instanceof Defaultable) {
            return ((Defaultable<T>) value).get();
        }
        return (T) value;
    }

    protected boolean isDefaultable() {
        return Defaultable.class.isAssignableFrom(this.field.getType());
    }
}
