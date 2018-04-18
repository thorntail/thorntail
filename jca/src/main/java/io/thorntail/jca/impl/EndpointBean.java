package io.thorntail.jca.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * Created by bob on 2/21/18.
 */
public class EndpointBean implements Bean {
    public EndpointBean(Class<?> instanceType, Class<?> listenerInterface, InjectionTarget injectionTarget) {
        this.instanceType = instanceType;
        this.listenerInterface = listenerInterface;
        this.injectionTarget = injectionTarget;
    }

    @Override
    public Class<?> getBeanClass() {
        return this.instanceType;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return this.injectionTarget.getInjectionPoints();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Object create(CreationalContext creationalContext) {
        Object instance = this.injectionTarget.produce(creationalContext);
        this.injectionTarget.inject(instance, creationalContext);
        this.injectionTarget.postConstruct(instance);
        return instance;
    }


    @Override
    public void destroy(Object instance, CreationalContext creationalContext) {
        this.injectionTarget.preDestroy(instance);
        this.injectionTarget.dispose(instance);
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<>();
        types.add(this.instanceType);
        types.add(this.listenerInterface);
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.emptySet();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    private final Class<?> listenerInterface;

    private final Class<?> instanceType;

    private final InjectionTarget injectionTarget;
}
