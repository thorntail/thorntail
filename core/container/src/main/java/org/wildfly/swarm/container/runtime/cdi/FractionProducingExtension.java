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
package org.wildfly.swarm.container.runtime.cdi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.container.runtime.ConfigurableManager;
import org.wildfly.swarm.container.runtime.cdi.configurable.ConfigurableFractionBean;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Ken Finnigan
 */
public class FractionProducingExtension implements Extension {

    private final List<Fraction> explicitlyInstalledFractions = new ArrayList<>();

    private final ConfigurableManager configurableManager;

    public FractionProducingExtension(Collection<Fraction> explicitlyInstalled, ConfigurableManager configurableManager) {
        this.explicitlyInstalledFractions.addAll(explicitlyInstalled);
        this.configurableManager = configurableManager;
    }

    /**
     * Once all beans have been discovered by Weld, for each custom fraction that we have,
     * add the Bean instance to Weld as a replacement for the @DefaultFraction instance we vetoed.
     *
     * @param abd AfterBeanDiscovery
     */
    @SuppressWarnings("unused")
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) throws Exception {
        try (AutoCloseable handle = Performance.time("FractionProducingExtension.afterBeanDiscovery")) {
            Set<Type> preExistingFractionClasses = new HashSet<>();

            try (AutoCloseable pre = Performance.time("FractionProducingExtension.afterBeanDiscovery - pre-existing")) {
                for (Fraction<?> fraction : explicitlyInstalledFractions) {
                    try {
                        abd.addBean(new ConfigurableFractionBean<>(fraction, this.configurableManager));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    preExistingFractionClasses.add(fraction.getClass());
                }

                Set<Bean<?>> availableFractionBeans = beanManager.getBeans(Fraction.class, Any.Literal.INSTANCE);

                preExistingFractionClasses.addAll(
                        availableFractionBeans.stream()
                                .flatMap(e -> e.getTypes().stream())
                                .collect(Collectors.toSet())

                );
            }

            Set<Class<? extends Fraction>> fractionClasses = uninstalledFractionClasses(preExistingFractionClasses);

            try (AutoCloseable defaultHandle = Performance.time("FractionProducingExtension.afterBeanDiscovery - default")) {
                fractionClasses.forEach((cls) -> {
                    try {
                        abd.addBean(new ConfigurableFractionBean<>(cls, this.configurableManager));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends Fraction>> uninstalledFractionClasses(Set<Type> installedClasses) throws ModuleLoadException, IOException, ClassNotFoundException {

        Set<String> installedClassNames = installedClasses.stream().map(Type::getTypeName).collect(Collectors.toSet());

        List<String> moduleNames = ApplicationEnvironment.get().bootstrapModules();

        ClassLoader cl = Module.getBootModuleLoader().loadModule("swarm.container").getClassLoader();

        Set<Class<? extends Fraction>> fractionClasses = new HashSet<>();

        for (String moduleName : moduleNames) {
            Module module = Module.getBootModuleLoader().loadModule(moduleName);

            InputStream indexStream = module.getClassLoader().getResourceAsStream("META-INF/jandex.idx");
            if (indexStream != null) {
                IndexReader reader = new IndexReader(indexStream);
                Index index = reader.read();
                Set<ClassInfo> impls = index.getAllKnownImplementors(DotName.createSimple(Fraction.class.getName()));
                for (ClassInfo impl : impls) {
                    if (!installedClassNames.contains(impl.name().toString())) {
                        Class<? extends Fraction> fractionClass = (Class<? extends Fraction>) cl.loadClass(impl.name().toString());
                        fractionClasses.add(fractionClass);
                    }
                }
            }
        }
        return fractionClasses;
    }
}
