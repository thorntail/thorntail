package org.wildfly.swarm.container.runtime.cdi.configurable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;

import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.wildfly.swarm.container.runtime.ConfigurableManager;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Bob McWhirter
 */
public class ConfigurableFractionBean<T extends Fraction> implements Bean<T> {

    private final T instance;

    public ConfigurableFractionBean(T instance, ConfigurableManager configurableManager) throws Exception {
        this.instance = instance;
        configurableManager.scan(this.instance);
    }

    public ConfigurableFractionBean(Class<T> cls, ConfigurableManager configurableManager) throws Exception {
        this.instance = cls.newInstance();
        this.instance.applyDefaults();
        configurableManager.scan(this.instance);
    }

    @Override
    public Class<?> getBeanClass() {
        return this.instance.getClass();
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
        return this.instance;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        // no-op
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> types = applicableClasses(this.instance.getClass());
        return types;
    }

    Set<Type> applicableClasses(Class cur) {
        Set<Type> classes = new HashSet<>();
        applicableClasses(cur, classes);
        return classes;
    }

    void applicableClasses(Class cur, Set<Type> set) {
        if (cur == null) {
            return;
        }

        set.add(cur);

        for (Class each : cur.getInterfaces()) {
            applicableClasses(each, set);
        }

        applicableClasses(cur.getSuperclass(), set);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.add(DefaultLiteral.INSTANCE);
        qualifiers.add(AnyLiteral.INSTANCE);
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
