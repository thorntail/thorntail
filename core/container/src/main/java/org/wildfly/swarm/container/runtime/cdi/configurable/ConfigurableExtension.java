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
package org.wildfly.swarm.container.runtime.cdi.configurable;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.inject.Singleton;

import org.wildfly.swarm.container.runtime.ConfigurableManager;
import org.wildfly.swarm.spi.api.ArchiveMetadataProcessor;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.spi.api.Customizer;

/**
 * @author Ken Finnigan
 */
public class ConfigurableExtension implements Extension {

    private final ConfigurableManager configurableManager;

    private static final Set<Class<?>> APPLICABLE_CLASSES = new HashSet<Class<?>>() {{
        add(Customizer.class);
        add(ArchivePreparer.class);
        add(ArchiveMetadataProcessor.class);
    }};

    public ConfigurableExtension(ConfigurableManager configurableManager) {
        this.configurableManager = configurableManager;
    }

    void processInjectionTarget(@Observes ProcessInjectionTarget pit, BeanManager beanManager) throws InstantiationException, IllegalAccessException {
        if ( isApplicable( pit.getAnnotatedType().getJavaClass() ) ) {
            pit.setInjectionTarget(new ConfigurableInjectionTarget<>(pit.getInjectionTarget(), this.configurableManager));
        }
    }

    static boolean isApplicable(Class<?> cls) {
        for (Class<?> each : APPLICABLE_CLASSES) {
            if ( each.isAssignableFrom( cls ) ) {
                return true;
            }
        }

        return false;
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        abd.addBean()
                .types(ConfigurableManager.class)
                .scope(Singleton.class)
                .qualifiers(Default.Literal.INSTANCE)
                .producing(this.configurableManager);
    }

}
