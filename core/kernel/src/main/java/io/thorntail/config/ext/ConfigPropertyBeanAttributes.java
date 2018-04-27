package io.thorntail.config.ext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;

/**
 * Created by bob on 2/2/18.
 */
class ConfigPropertyBeanAttributes<T> implements BeanAttributes<T> {

    private final BeanAttributes<?> delegate;

    public ConfigPropertyBeanAttributes(final BeanAttributes<?> delegate, Type type) {
        this.delegate = delegate;
        this.type = type;
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return this.delegate.getQualifiers();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return this.delegate.getScope();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return this.delegate.getStereotypes();
    }

    public Set<Type> getTypes() {
        final Set<Type> types = new HashSet<>();
        types.add(Object.class);
        types.add(this.type);
        return types;
    }

    @Override
    public boolean isAlternative() {
        return this.delegate.isAlternative();
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }

    private final Type type;
}
