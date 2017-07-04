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
package org.wildfly.swarm.container.runtime.cdi;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Named;

import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.NamedLiteral;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.spi.api.cdi.CommonBean;
import org.wildfly.swarm.spi.api.cdi.CommonBeanBuilder;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.SimpleKey;

/**
 * @author Bob McWhirter
 */
public class InterfaceExtension implements Extension {

    private static ConfigKey ROOT = ConfigKey.of("swarm", "network", "interfaces");

    public InterfaceExtension(ConfigView configView) {
        this.configView = configView;
    }

    @SuppressWarnings("unused")
    void process(@Observes ProcessProducer<?, Interface> p, BeanManager beanManager) throws Exception {
        p.getAnnotatedMember().getAnnotations()
                .stream()
                .filter(e -> e instanceof Named)
                .findFirst()
                .ifPresent(anno -> {
                    String simpleName = ((Named) anno).value().replace("-interface", "");
                    String bind = (String) configView.valueOf(ROOT.append(simpleName).append("bind"));
                    if (bind == null) {
                        // nothing to do;
                        return;
                    }

                    Producer<Interface> originalProducer = p.getProducer();

                    p.setProducer(new Producer<Interface>() {
                        @Override
                        public Interface produce(CreationalContext<Interface> ctx) {
                            Interface instance = originalProducer.produce(ctx);
                            instance.setExpression(bind);
                            return instance;
                        }

                        @Override
                        public void dispose(Interface instance) {
                            originalProducer.dispose(instance);
                        }

                        @Override
                        public Set<InjectionPoint> getInjectionPoints() {
                            return originalProducer.getInjectionPoints();
                        }
                    });
                });
    }

    @SuppressWarnings("unused")
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) throws Exception {

        List<SimpleKey> configuredInterfaces = this.configView.simpleSubkeys(ROOT);

        for (SimpleKey interfaceName : configuredInterfaces) {

            Set<Bean<?>> ifaces = beanManager.getBeans(Interface.class, AnyLiteral.INSTANCE);

            AtomicBoolean producerRequired = new AtomicBoolean(false);

            if (ifaces
                    .stream()
                    .noneMatch(e -> e.getQualifiers()
                            .stream()
                            .anyMatch(anno -> anno instanceof Named && ((Named) anno).value().equals(interfaceName + "-interface")))) {

                Interface iface = new Interface(interfaceName.name(), "0.0.0.0");

                String bind = (String) this.configView.valueOf(ROOT.append(interfaceName).append("bind"));
                if (bind != null) {
                    iface.setExpression(bind);
                }

                if (producerRequired.get()) {
                    CommonBean<Interface> interfaceBean = CommonBeanBuilder.newBuilder(Interface.class)
                            .beanClass(InterfaceExtension.class)
                            .scope(ApplicationScoped.class)
                            .addQualifier(AnyLiteral.INSTANCE)
                            .addQualifier(new NamedLiteral(interfaceName.name() + "-interface"))
                            .createSupplier(() -> iface)
                            .addType(Interface.class)
                            .addType(Object.class)
                            .build();

                    abd.addBean(interfaceBean);
                }
            }
        }
    }

    private final ConfigView configView;
}
