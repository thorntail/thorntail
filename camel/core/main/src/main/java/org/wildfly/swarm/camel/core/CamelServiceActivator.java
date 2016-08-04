package org.wildfly.swarm.camel.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
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
import org.wildfly.extension.camel.service.CamelContextRegistryService;

import static org.wildfly.swarm.camel.core.AbstractCamelFraction.LOGGER;

/**
 * @author Bob McWhirter
 */
@Singleton
public class CamelServiceActivator implements ServiceActivator {

    @Inject
    @Any
    private CamelCoreFraction fraction;

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        BootstrapCamelContextService.addService(context.getServiceTarget(), this.fraction);
    }

    static class BootstrapCamelContextService extends AbstractService<Void> {

        static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("wildfly", "swarm", "camel", "bootstrap");

        InjectedValue<CamelContextRegistryService.MutableCamelContextRegistry> injectedContextRegistry = new InjectedValue<>();

        List<CamelContext> systemContexts = new ArrayList<>();

        CamelCoreFraction fraction;

        static ServiceController<Void> addService(ServiceTarget serviceTarget, CamelCoreFraction fraction) {
            BootstrapCamelContextService service = new BootstrapCamelContextService(fraction);
            ServiceName serviceName = SERVICE_NAME;
            ServiceBuilder<Void> builder = serviceTarget.addService(serviceName, service);
            builder.addDependency(CamelConstants.CAMEL_CONTEXT_REGISTRY_SERVICE_NAME, CamelContextRegistryService.MutableCamelContextRegistry.class, service.injectedContextRegistry);
            return builder.install();
        }

        BootstrapCamelContextService(CamelCoreFraction fraction) {
            this.fraction = fraction;
        }

        @Override
        public void start(StartContext startContext) throws StartException {
            CamelContextRegistryService.MutableCamelContextRegistry contextRegistry = injectedContextRegistry.getValue();
            ClassLoader classLoader = CamelContextRegistryService.MutableCamelContextRegistry.class.getClassLoader();
            try {
                for (RouteBuilder builder : this.fraction.getRouteBuilders()) {
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
            CamelContextRegistryService.MutableCamelContextRegistry contextRegistry = injectedContextRegistry.getValue();
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
