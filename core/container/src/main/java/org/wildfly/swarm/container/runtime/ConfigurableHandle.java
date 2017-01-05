package org.wildfly.swarm.container.runtime;

import java.lang.reflect.Field;

import org.wildfly.swarm.spi.api.Defaultable;

/**
 * @author Bob McWhirter
 */
public class ConfigurableHandle {

    private final String name;

    private final Object instance;

    private final Field field;

    public ConfigurableHandle(String name, Object instance, Field field) {
        this.name = name;
        this.instance = instance;
        this.field = field;
        this.field.setAccessible(true);
    }

    public String name() {
        return this.name;
    }

    public Class<?> type() throws IllegalAccessException {
        if (isDefaultable()) {
            return ((Defaultable) this.field.get(this.instance)).type();
        }

        return this.field.getType();
    }

    public <T> void set(T value) throws IllegalAccessException {
        if (isDefaultable()) {
            ((Defaultable) this.field.get(this.instance)).set(value);
        } else {
            this.field.set(this.instance, value);
        }
    }

    protected <T> T currentValue() throws IllegalAccessException {
        Object value = this.field.get(this.instance);
        if (value instanceof Defaultable) {
            return (T) ((Defaultable) value).get();
        }
        return (T) value;
    }

    protected boolean isDefaultable() {
        return Defaultable.class.isAssignableFrom(this.field.getType());
    }
}
