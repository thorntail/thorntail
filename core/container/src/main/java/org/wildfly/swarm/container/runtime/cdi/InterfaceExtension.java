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

import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;

import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.spi.api.cdi.CommonBean;
import org.wildfly.swarm.spi.api.cdi.CommonBeanBuilder;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.SimpleKey;

/**
 * @author Bob McWhirter
 */
public class InterfaceExtension extends AbstractNetworkExtension<Interface> {

    private static ConfigKey ROOT = ConfigKey.of("thorntail", "network", "interfaces");

    public InterfaceExtension(ConfigView configView) {
        super(configView);
    }

    @Override
    protected void applyConfiguration(Interface instance) {
        ConfigKey key = ROOT.append(instance.getName().replace("-interface", ""));
        applyConfiguration(key.append("bind"), (bind) -> {
            instance.setExpression(bind.toString());
        });
    }

    @SuppressWarnings("unused")
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) throws Exception {

        List<SimpleKey> configuredInterfaces = this.configView.simpleSubkeys(ROOT);

        for (SimpleKey interfaceName : configuredInterfaces) {

            Set<Bean<?>> ifaces = beanManager.getBeans(Interface.class, Any.Literal.INSTANCE);

            if (ifaces
                    .stream()
                    .noneMatch(e -> e.getQualifiers()
                            .stream()
                            .anyMatch(anno -> anno instanceof Named && ((Named) anno).value().equals(interfaceName + "-interface")))) {

                Interface iface = new Interface(interfaceName.name(), "0.0.0.0");
                applyConfiguration(iface);
                CommonBean<Interface> interfaceBean = CommonBeanBuilder.newBuilder(Interface.class)
                            .beanClass(InterfaceExtension.class)
                            .scope(ApplicationScoped.class)
                            .addQualifier(Any.Literal.INSTANCE)
                            .addQualifier(NamedLiteral.of(interfaceName.name() + "-interface"))
                            .createSupplier(() -> iface)
                            .addType(Interface.class)
                            .addType(Object.class)
                            .build();

                abd.addBean(interfaceBean);
            }
        }
    }

}
