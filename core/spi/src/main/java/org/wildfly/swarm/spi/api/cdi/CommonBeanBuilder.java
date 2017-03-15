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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Tomas Remes
 */
public class CommonBeanBuilder<T> {

    private Class<?> beanClass;
    private Class<? extends Annotation> scope;
    private Set<Annotation> qualifiers;
    private Supplier<T> createSupplier;
    private Set<Type> types;

    CommonBeanBuilder() {
        this.qualifiers = new HashSet<>();
        this.types = new HashSet<>();
    }

    public static <B> CommonBeanBuilder<B> newBuilder(Class<B> beanClass) {
        return new CommonBeanBuilder<B>();
    }

    public CommonBeanBuilder<T> beanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
        return this;
    }

    public CommonBeanBuilder<T> scope(Class<? extends Annotation> scope) {
        this.scope = scope;
        return this;
    }

    public CommonBeanBuilder<T> addQualifier(Annotation qualifier) {
        qualifiers.add(qualifier);
        return this;
    }

    public CommonBeanBuilder<T> createSupplier(Supplier<T> createSupplier) {
        this.createSupplier = createSupplier;
        return this;
    }

    public CommonBeanBuilder<T> addType(Type type) {
        types.add(type);
        return this;
    }

    public CommonBean<T> build() {
        return new CommonBean<>(beanClass, scope, qualifiers, createSupplier, types.toArray(new Type[types.size()]));
    }
}
