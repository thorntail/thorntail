/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
