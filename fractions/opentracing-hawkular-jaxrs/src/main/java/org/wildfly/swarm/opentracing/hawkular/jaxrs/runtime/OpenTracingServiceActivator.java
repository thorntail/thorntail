package org.wildfly.swarm.opentracing.hawkular.jaxrs.runtime;

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
import org.wildfly.swarm.opentracing.hawkular.jaxrs.OpenTracingHawkularFraction;
import org.wildfly.swarm.opentracing.hawkular.jaxrs.TracerLookup;

/**
 * @author Pavol Loffay
 */
public class OpenTracingServiceActivator implements ServiceActivator {

    @Inject
    @Any
    private Instance<OpenTracingHawkularFraction> openTracingHawkularFraction;

    @Override
    public void activate(ServiceActivatorContext serviceActivatorContext) throws ServiceRegistryException {
        ServiceTarget target = serviceActivatorContext.getServiceTarget();

        OpenTracingHawkularJaxRsService openTracingHawkularJaxRsService =
                new OpenTracingHawkularJaxRsService(openTracingHawkularFraction.get().getJaxrsTraceBuilder());

        ServiceBuilder<OpenTracingHawkularJaxRsService> serviceBuilder = target.addService(
                OpenTracingHawkularJaxRsService.SERVICE_NAME,
                openTracingHawkularJaxRsService);

        serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE).install();

        BinderService binderService = new BinderService(TracerLookup.JNDI_NAME, null, true);

        target.addService(ContextNames.buildServiceName(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, TracerLookup.JNDI_NAME),
                binderService)
                .addDependency(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                .addInjection(binderService.getManagedObjectInjector(), new ImmediateManagedReferenceFactory(
                        openTracingHawkularJaxRsService))
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }
}
