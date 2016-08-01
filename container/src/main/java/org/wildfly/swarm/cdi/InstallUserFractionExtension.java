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
package org.wildfly.swarm.cdi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.inject.spi.builder.BeanConfigurator;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.DefaultFraction;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Ken Finnigan
 */
public class InstallUserFractionExtension implements Extension {
    public static Map<Class<?>, Fraction> userFractions = new HashMap<>();

    /**
     * Find all Fraction types with @DefaultFraction that are discovered by Weld and veto those
     * where we have a custom Fraction that was added.
     *
     * @param pat ProcessAnnotatedType
     */
    <T> void processAnnotatedType(@Observes @WithAnnotations(DefaultFraction.class) ProcessAnnotatedType<T> pat) {
        if (userFractions.containsKey(pat.getAnnotatedType().getJavaClass())) {
            pat.veto();
        }
    }

    /**
     * Once all beans have been discovered by Weld, for each custom fraction that we have,
     * add the Bean instance to Weld as a replacement for the @DefaultFraction instance we vetoed.
     *
     * @param abd AfterBeanDiscovery
     */
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {
        for (Fraction fraction : userFractions.values()) {
            BeanConfigurator<Object> configurator = abd.addBean()
                    .addType(fraction.getClass())
                    .addType(Fraction.class)
                    .scope(Singleton.class)
                    .produceWith(() -> fraction );

            configurator.qualifiers(
                    Arrays.asList(fraction.getClass().getAnnotations())
                            .stream()
                            .filter(p -> (!p.annotationType().equals(DefaultFraction.class)
                                    && !p.annotationType().equals(ApplicationScoped.class)
                                    && !p.annotationType().equals(Singleton.class)))
                            .collect(Collectors.toSet())
            );
        }
    }
}
