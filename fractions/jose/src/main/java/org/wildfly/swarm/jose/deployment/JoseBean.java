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
package org.wildfly.swarm.jose.deployment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseFraction;

/**
 *
 */
@Vetoed
public class JoseBean implements Bean<Jose> {

    private JoseFraction jose;

    public JoseBean(JoseFraction jose) {
        this.jose = jose;
    }

    @Override
    public Class<?> getBeanClass() {
        return Jose.class;
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
        Set<Type> types = new HashSet<>();
        types.add(Jose.class);
        types.add(Object.class);
        return types;
    }

    @SuppressWarnings("serial")
    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.singleton(new AnnotationLiteral<Default>() {
        });
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

    @Override
    public Jose create(CreationalContext<Jose> creationalContext) {
        return this.jose.getJoseInstance();
    }

    @Override
    public void destroy(Jose jose, CreationalContext<Jose> creationalContext) {

    }
}
