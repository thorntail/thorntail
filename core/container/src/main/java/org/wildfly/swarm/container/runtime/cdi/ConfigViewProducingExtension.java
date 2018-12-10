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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Singleton;

import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.spi.api.cdi.CommonBean;
import org.wildfly.swarm.spi.api.cdi.CommonBeanBuilder;
import org.wildfly.swarm.spi.api.config.ConfigView;

/**
 * Produces an explicitly set {@link ConfigView}
 *
 * @author Bob McWhirter
 */
public class ConfigViewProducingExtension implements Extension {

    private final ConfigView configView;

    public ConfigViewProducingExtension(ConfigView configView) {
        this.configView = configView;
    }

    @SuppressWarnings("unused")
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) throws Exception {
        try (AutoCloseable handle = Performance.time("ConfigViewProducingExtension.afterBeanDiscovery")) {
            CommonBean<ConfigView> configViewBean = CommonBeanBuilder.newBuilder(ConfigView.class)
                    .beanClass(ConfigViewProducingExtension.class)
                    .scope(Singleton.class)
                    .addQualifier(Default.Literal.INSTANCE)
                    .createSupplier(() -> configView)
                    .addType(ConfigView.class)
                    .addType(Object.class).build();
            abd.addBean(configViewBean);
        }
    }

    public ConfigView getConfigView() {
        return this.configView;
    }

}
