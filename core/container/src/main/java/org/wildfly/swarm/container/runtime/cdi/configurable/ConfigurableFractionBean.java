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
package org.wildfly.swarm.container.runtime.cdi.configurable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;

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
        this.instance.applyDefaults(configurableManager.hasConfiguration(this.instance));
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
        qualifiers.add(Default.Literal.INSTANCE);
        qualifiers.add(Any.Literal.INSTANCE);
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
