package org.wildfly.swarm.camel.core.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ExplicitCamelContextNameStrategy;
import org.apache.camel.model.ModelCamelContext;
import org.jboss.modules.Module;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extension.camel.CamelConstants;
import org.wildfly.extension.camel.handler.ModuleClassLoaderAssociationHandler;
import org.wildfly.swarm.camel.core.CamelFraction;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class CamelServiceActivator implements ServiceActivator {
    public static final Logger LOGGER = LoggerFactory.getLogger("org.wildfly.swarm.camel");

    @Inject
    @Any
    private CamelFraction fraction;

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        BootstrapCamelContextService.addService(context.getServiceTarget(), this.fraction);
    }

    static class BootstrapCamelContextService extends AbstractService<Void> {

        static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("wildfly", "swarm", "camel", "bootstrap");

        List<CamelContext> systemContexts = new ArrayList<>();

        CamelFraction fraction;

        static ServiceController<Void> addService(ServiceTarget serviceTarget, CamelFraction fraction) {
            BootstrapCamelContextService service = new BootstrapCamelContextService(fraction);
            ServiceName serviceName = SERVICE_NAME;
            ServiceBuilder<Void> builder = serviceTarget.addService(serviceName, service);
            builder.addDependency(CamelConstants.CAMEL_CONTEXT_REGISTRY_SERVICE_NAME);
            return builder.install();
        }

        BootstrapCamelContextService(CamelFraction fraction) {
            this.fraction = fraction;
        }

        @Override
        public void start(StartContext startContext) throws StartException {
            try {
                Module appModule = Module.getCallerModuleLoader().loadModule("thorntail.application");
                ModuleClassLoaderAssociationHandler.associate(appModule.getClassLoader());
                try {
                    for (Map.Entry<String, RouteBuilder> entry : fraction.getRouteBuilders().entrySet()) {
                        String name = entry.getKey();
                        RouteBuilder builder = entry.getValue();
                        ModelCamelContext camelctx = builder.getContext();
                        if (name != null) {
                            camelctx.setNameStrategy(new ExplicitCamelContextNameStrategy(name));
                        }
                        builder.addRoutesToCamelContext(camelctx);
                        systemContexts.add(camelctx);
                    }
                } finally {
                    ModuleClassLoaderAssociationHandler.disassociate();
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
            Collections.reverse(systemContexts);
            Iterator<CamelContext> iterator = systemContexts.iterator();
            while (iterator.hasNext()) {
                CamelContext camelctx = iterator.next();
                try {
                    camelctx.stop();
                } catch (Exception ex) {
                    LOGGER.error("Cannot stop context: " + camelctx, ex);
                }
                iterator.remove();
            }
        }
    }
}
