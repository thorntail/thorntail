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
package org.wildfly.swarm.spi.api.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * @author Tomas Remes
 */
public class CommonBean<T> implements Bean<T> {

    private final Class<?> beanClass;
    private final Class<? extends Annotation> scope;
    private final Set<Annotation> qualifiers;
    private final Set<Type> types;
    private final Supplier<T> createSupplier;

    public CommonBean(Class<?> beanClass, Class<? extends Annotation> scope, Set<Annotation> qualifiers, Supplier<T> createSupplier,
            Type... types) {
        this.beanClass = beanClass;
        this.scope = scope;
        this.qualifiers = qualifiers;
        this.types = new HashSet<>(Arrays.asList(types));
        this.createSupplier = createSupplier;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
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
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
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

    @Override
    public T create(CreationalContext<T> creationalContext) {
        return createSupplier != null ? createSupplier.get() : null;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        creationalContext.release();
    }
}
