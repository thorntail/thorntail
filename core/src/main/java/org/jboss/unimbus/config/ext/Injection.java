package org.jboss.unimbus.config.ext;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;

import org.jboss.unimbus.config.impl.ConfigImpl;

/**
 * Created by bob on 2/2/18.
 */
class Injection {

    Injection(String name, Type injectionType, String defaultValue) {
        this.injectionType = injectionType;
        this.name = name;
        this.defaultValue = defaultValue;
        this.coercer = new InjectionCoercer(this.injectionType);
    }

    String getName() {
        return this.name;
    }

    Type getInjectionType() {
        return this.injectionType;
    }

    String getDefaultValue() {
        return this.defaultValue;
    }

    InjectionCoercer getCoercer() {
        return this.coercer;
    }

    Object produce(ConfigImpl config) throws InstantiationException, IllegalAccessException {
        return this.coercer.coerce(config, this.name, this.defaultValue);
    }

    private final InjectionCoercer coercer;

    private String name;

    private Type injectionType;

    private String defaultValue;
}
