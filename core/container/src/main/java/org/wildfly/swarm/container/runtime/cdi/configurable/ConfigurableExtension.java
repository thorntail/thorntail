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

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.inject.Singleton;

import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.container.runtime.ConfigurableManager;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.cdi.CommonBean;
import org.wildfly.swarm.spi.api.cdi.CommonBeanBuilder;

/**
 * @author Ken Finnigan
 */
public class ConfigurableExtension implements Extension {

    private final ConfigurableManager configurableManager;

    // Short-cut for-sure expect these to be Configurable owners.
    private static final Set<Class<?>> APPLICABLE_CLASSES = new HashSet<Class<?>>() {{
        add(Customizer.class);
        add(DeploymentProcessor.class);
    }};

    public ConfigurableExtension(ConfigurableManager configurableManager) {
        this.configurableManager = configurableManager;
    }

    @SuppressWarnings("unused")
    <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> pit, BeanManager beanManager) throws Exception {
        try (AutoCloseable handle = Performance.accumulate("ConfigurationExtension.processInjectionTarget")) {
            if (isApplicable(pit.getAnnotatedType())) {
                pit.setInjectionTarget(new ConfigurableInjectionTarget<T>(pit.getInjectionTarget(), this.configurableManager));
            }
        }
    }

    private static <T> boolean isApplicable(AnnotatedType<T> at) {
        if (isApplicable(at.getJavaClass())) {
            return true;
        }

        Set<AnnotatedField<? super T>> fields = at.getFields();

        for (AnnotatedField<? super T> field : fields) {
            if (field.isAnnotationPresent(Configurable.class)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isApplicable(Class<?> cls) {
        for (Class<?> each : APPLICABLE_CLASSES) {
            if (each.isAssignableFrom(cls)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings({"unused"})
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) throws Exception {
        try (AutoCloseable handle = Performance.time("ConfigurationExtension.afterBeanDiscovery")) {
            CommonBean<ConfigurableManager> configurableManagerBean = CommonBeanBuilder.newBuilder(ConfigurableManager.class)
                    .beanClass(ConfigurableManager.class)
                    .scope(Singleton.class)
                    .addQualifier(Default.Literal.INSTANCE)
                    .createSupplier(() -> configurableManager)
                    .addType(ConfigurableManager.class)
                    .addType(Object.class).build();
            abd.addBean(configurableManagerBean);
        }
    }

}
