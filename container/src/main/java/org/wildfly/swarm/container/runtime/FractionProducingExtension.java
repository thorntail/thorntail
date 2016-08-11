/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.container.runtime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Ken Finnigan
 */
public class FractionProducingExtension implements Extension {

    private Set<Class<? extends Fraction>> fractionClasses = new HashSet<>();

    private List<Fraction> explicitlyInstalledFractions = new ArrayList<>();

    public FractionProducingExtension(Collection<Fraction> explicitlyInstalled) {
        this.explicitlyInstalledFractions.addAll(explicitlyInstalled);
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
        Class<?> cls = pat.getAnnotatedType().getJavaClass();

        if (Fraction.class.isAssignableFrom(cls)) {
            pat.veto();
            if (cls == Fraction.class) {
                return;
            }

            this.fractionClasses.add((Class<? extends Fraction>) cls);
        }
    }

    /**
     * Once all beans have been discovered by Weld, for each custom fraction that we have,
     * add the Bean instance to Weld as a replacement for the @DefaultFraction instance we vetoed.
     *
     * @param abd AfterBeanDiscovery
     */
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {
        Set<Type> preExistingFractionClasses = new HashSet<>();

        for (Fraction fraction : explicitlyInstalledFractions) {
            abd.addBean()
                    .addTypes(applicableClasses(fraction.getClass()))
                    .scope(Singleton.class)
                    .addQualifier(new AnnotationLiteral<Default>() {
                    })
                    .produceWith(() -> fraction);

            preExistingFractionClasses.add(fraction.getClass());
        }

        Set<Bean<?>> availableFractionBeans = beanManager.getBeans(Fraction.class, new AnnotationLiteral<Any>() {
        });

        preExistingFractionClasses.addAll(
                availableFractionBeans.stream()
                        .flatMap(e -> e.getTypes().stream())
                        .collect(Collectors.toSet())

        );

        fractionClasses.stream()
                .filter(cls -> !preExistingFractionClasses.contains(cls))
                .forEach((cls) -> {
                    abd.addBean()
                            .addTypes(applicableClasses(cls))
                            .scope(Singleton.class)
                            .addQualifier(new AnnotationLiteral<Default>() {
                            })
                            .produceWith(() -> {
                                try {
                                    return cls.newInstance().applyDefaults();
                                } catch (InstantiationException | IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                });
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
}
