package io.thorntail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import io.thorntail.util.Types;
import io.thorntail.util.Annotations;

/**
 * Created by bob on 2/21/18.
 */
class InstanceBean<T> implements Bean<T> {
    InstanceBean(InjectionTarget<T> injectionTarget, Class<T> instanceType, T base) {
        this.injectionTarget = injectionTarget;
        this.instanceType = instanceType;
        this.base = base;
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
    public T create(CreationalContext<T> creationalContext) {
        T instance = this.injectionTarget.produce(creationalContext);
        this.injectionTarget.inject(instance, creationalContext);
        this.injectionTarget.postConstruct(instance);
        return instance;
    }


    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        this.injectionTarget.preDestroy(instance);
        this.injectionTarget.dispose(instance);
    }

    @Override
    public Set<Type> getTypes() {
        return Types.getTypeClosure(this.instanceType);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> set = new HashSet<>();
        set.addAll(Annotations.getQualifiers(this.instanceType) );
        if ( set.isEmpty() ) {
            set.add(Default.Literal.INSTANCE);
            set.add(Any.Literal.INSTANCE);
        }
        return set;
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

    private final Class<T> instanceType;

    private final InjectionTarget<T> injectionTarget;

    private final T base;
}
