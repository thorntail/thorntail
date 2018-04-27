package io.thorntail.config.ext;

import java.lang.reflect.Type;

import io.thorntail.config.impl.ConfigImpl;

/**
 * Created by bob on 2/2/18.
 */
class Injection {

    Injection(String name, Type injectionType, String defaultValue) {
        this.injectionType = injectionType;
        this.name = name;
        this.defaultValue = defaultValue;
        this.coercer = InjectionCoercer.of(this.injectionType);
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
