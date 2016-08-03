package org.wildfly.swarm.container.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

/**
 * @author Bob McWhirter
 */
public class SimpleExposedBean<T> implements Bean<T> {
    private final Class<T> cls;
    private final T bean;

    public SimpleExposedBean(Class<T> cls, T bean) {
        this.cls = cls;
        this.bean = bean;
    }

    @Override
    public Class<?> getBeanClass() {
        return this.cls;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        return this.bean;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        // no-op
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<>();
        types.add( this.cls );
        types.add( Object.class);
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.add(new AnnotationLiteral<Any>() { });
        qualifiers.add(new AnnotationLiteral<Default>() { });
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Singleton.class;
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
}
