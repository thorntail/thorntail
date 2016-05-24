/*
 * #%L
 * Camel Core :: Main
 * %%
 * Copyright (C) 2016 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wildfly.swarm.camel.core.runtime;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.wildfly.swarm.camel.core.CamelCoreFraction.LOGGER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.camel.CamelConstants;
import org.wildfly.extension.camel.service.CamelContextRegistryService.MutableCamelContextRegistry;
import org.wildfly.swarm.camel.core.CamelCoreFraction;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;

public class CamelCoreConfiguration extends AbstractServerConfiguration<CamelCoreFraction> {

    public CamelCoreConfiguration() {
        super(CamelCoreFraction.class);
    }

    @Override
    public CamelCoreFraction defaultFraction() {
        return new CamelCoreFraction();
    }

    @Override
    public List<ModelNode> getList(CamelCoreFraction fraction) throws Exception {

        ModelNode node = new ModelNode();
        List<ModelNode> list = new ArrayList<>();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.camel");
        node.get(OP).set(ADD);
        list.add(node);

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "camel"));

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        return list;
    }

    @Override
    public List<ServiceActivator> getServiceActivators(final CamelCoreFraction fraction) {
        List<ServiceActivator> activators = new ArrayList<>(super.getServiceActivators(fraction));
        activators.add(new ServiceActivator() {
            @Override
            public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
                BootstrapCamelContextService.addService(context.getServiceTarget(), fraction);
            }
        });
        return activators;
    }

    static class BootstrapCamelContextService extends AbstractService<Void> {

        static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("wildfly", "swarm", "camel", "bootstrap");

        InjectedValue<MutableCamelContextRegistry> injectedContextRegistry = new InjectedValue<>();
        List<CamelContext> systemContexts = new ArrayList<>();
        CamelCoreFraction fraction;

        static ServiceController<Void> addService(ServiceTarget serviceTarget, CamelCoreFraction fraction) {
            BootstrapCamelContextService service = new BootstrapCamelContextService(fraction);
            ServiceName serviceName = SERVICE_NAME;
            ServiceBuilder<Void> builder = serviceTarget.addService(serviceName, service);
            builder.addDependency(CamelConstants.CAMEL_CONTEXT_REGISTRY_SERVICE_NAME, MutableCamelContextRegistry.class, service.injectedContextRegistry);
            return builder.install();
        }

        BootstrapCamelContextService(CamelCoreFraction fraction) {
            this.fraction = fraction;
        }

        @Override
        public void start(StartContext startContext) throws StartException {
            MutableCamelContextRegistry contextRegistry = injectedContextRegistry.getValue();
            ClassLoader classLoader = MutableCamelContextRegistry.class.getClassLoader();
            try {
                for (RouteBuilder builder : fraction.getRouteBuilders()) {
                    ModelCamelContext camelctx = builder.getContext();
                    camelctx.setApplicationContextClassLoader(classLoader);
                    builder.addRoutesToCamelContext(camelctx);
                    contextRegistry.addCamelContext(camelctx);
                    systemContexts.add(camelctx);
                }
                for (CamelContext camelctx : systemContexts) {
                    camelctx.start();
                }
            } catch (Exception ex) {
                throw new StartException(ex);
            }
        }

        @Override
        public void stop(StopContext startContext) {
            MutableCamelContextRegistry contextRegistry = injectedContextRegistry.getValue();
            Collections.reverse(systemContexts);
            for (CamelContext camelctx : systemContexts) {
                try {
                    camelctx.stop();
                } catch (Exception ex) {
                    LOGGER.error("Cannot stop context: " + camelctx, ex);
                }
            }
            for (CamelContext camelctx : systemContexts) {
                contextRegistry.removeCamelContext(camelctx);
            }
        }
    }
}
