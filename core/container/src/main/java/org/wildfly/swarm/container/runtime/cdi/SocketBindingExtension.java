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
import java.util.function.Supplier;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Singleton;

import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.internal.SocketBindingRequest;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.cdi.CommonBean;
import org.wildfly.swarm.spi.api.cdi.CommonBeanBuilder;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Bob McWhirter
 */
public class SocketBindingExtension implements Extension {

    private final List<SocketBindingRequest> bindings;

    public SocketBindingExtension(List<SocketBindingRequest> bindings) {
        this.bindings = bindings;
    }

    @SuppressWarnings("unused")
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) throws Exception {
        try (AutoCloseable handle = Performance.time("SocketBindingExtension.afterBeanDiscovery")) {

            for (SocketBindingRequest each : this.bindings) {

                Supplier<Customizer> supplier = () -> (Customizer) () -> {
                    Set<Bean<?>> groups = beanManager.getBeans(SocketBindingGroup.class, Any.Literal.INSTANCE);

                    groups.stream()
                            .map((Bean<?> e) -> {
                                CreationalContext<?> ctx = beanManager.createCreationalContext(e);
                                return (SocketBindingGroup) beanManager.getReference(e, SocketBindingGroup.class, ctx);
                            })
                            .filter(group -> group.name().equals(each.socketBindingGroup()))
                            .findFirst()
                            .ifPresent((group) -> group.socketBinding(each.socketBinding()));
                };

                CommonBean<Customizer> customizerBean = CommonBeanBuilder.newBuilder(Customizer.class)
                        .beanClass(SocketBindingExtension.class)
                        .scope(Singleton.class)
                        .addQualifier(Pre.Literal.INSTANCE)
                        .addQualifier(Any.Literal.INSTANCE)
                        .createSupplier(supplier)
                        .addType(Customizer.class)
                        .addType(Object.class).build();
                abd.addBean(customizerBean);
            }
        }
    }
}
