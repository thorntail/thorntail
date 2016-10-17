package org.wildfly.swarm.jaxrs.btm.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.as.naming.ImmediateManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.jaxrs.btm.BraveLookup;
import org.wildfly.swarm.jaxrs.btm.ZipkinFraction;

/**
 * @author Heiko Braun
 */
@ApplicationScoped
public class BraveServiceActivator implements ServiceActivator {

    @Inject
    @Any
    Instance<ZipkinFraction> zipKinFractionInstance;

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {

        ServiceTarget target = context.getServiceTarget();

        BraveService service = new BraveService(zipKinFractionInstance.get().getBraveInstance());

        ServiceBuilder<BraveService> serviceBuilder = target.addService(BraveService.SERVICE_NAME, service);

        serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE).install();

        BinderService binderService = new BinderService(BraveLookup.JNDI_NAME, null, true);

        target.addService(ContextNames.buildServiceName(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, BraveLookup.JNDI_NAME), binderService)
                .addDependency(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                .addInjection(binderService.getManagedObjectInjector(), new ImmediateManagedReferenceFactory(service))
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();

    }

}
