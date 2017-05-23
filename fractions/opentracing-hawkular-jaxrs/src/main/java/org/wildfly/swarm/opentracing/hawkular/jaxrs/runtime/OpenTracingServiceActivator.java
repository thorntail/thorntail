package org.wildfly.swarm.opentracing.hawkular.jaxrs.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import io.opentracing.contrib.global.GlobalTracer;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import org.hawkular.apm.api.services.TracePublisher;
import org.hawkular.apm.client.api.recorder.BatchTraceRecorder;
import org.hawkular.apm.client.api.recorder.LoggingRecorder;
import org.hawkular.apm.client.api.recorder.TraceRecorder;
import org.hawkular.apm.client.api.sampler.PercentageSampler;
import org.hawkular.apm.client.api.sampler.Sampler;
import org.hawkular.apm.client.opentracing.APMTracer;
import org.hawkular.apm.client.opentracing.DeploymentMetaData;
import org.hawkular.apm.trace.publisher.rest.client.TracePublisherRESTClient;
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
import org.wildfly.swarm.opentracing.hawkular.jaxrs.HTTPTracePublisher;
import org.wildfly.swarm.opentracing.hawkular.jaxrs.OpenTracingHawkularFraction;
import org.wildfly.swarm.opentracing.hawkular.jaxrs.TracerLookup;
import org.wildfly.swarm.opentracing.hawkular.jaxrs.Tracing;

/**
 * @author Pavol Loffay
 */
@ApplicationScoped
public class OpenTracingServiceActivator implements ServiceActivator {

    @Inject
    @Any
    private OpenTracingHawkularFraction fraction;

    @Override
    public void activate(ServiceActivatorContext serviceActivatorContext) throws ServiceRegistryException {
        ServiceTarget target = serviceActivatorContext.getServiceTarget();

        OpenTracingHawkularJaxRsService openTracingHawkularJaxRsService = new OpenTracingHawkularJaxRsService(build(fraction.tracing()));

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

    public ServerTracingDynamicFeature.Builder build(Tracing tracing) {
        MultiTraceRecorder traceRecorder = new MultiTraceRecorder();

        if (tracing.consoleRecorder()) {
            traceRecorder.add(new LoggingRecorder());
        }

        if (tracing.traceRecorder() != null) {
            traceRecorder.add(build(tracing.traceRecorder()));
        }

        if (traceRecorder.isEmpty()) {
            traceRecorder.add(new BatchTraceRecorder());
        }

        Sampler sampler = PercentageSampler.withPercentage(tracing.sampleRate());
        DeploymentMetaData deploymentMetaData = new DeploymentMetaData(tracing.serviceName(), tracing.buildStamp());

        APMTracer apmTracer = new APMTracer(traceRecorder,
                                            sampler,
                                            deploymentMetaData);

        GlobalTracer.register(apmTracer);

        return ServerTracingDynamicFeature.Builder
                .traceAll(apmTracer);
    }

    public TraceRecorder build(org.wildfly.swarm.opentracing.hawkular.jaxrs.TraceRecorder recorder) {
        BatchTraceRecorder.BatchTraceRecorderBuilder builder = new BatchTraceRecorder.BatchTraceRecorderBuilder();
        builder.withBatchSize(recorder.batchSize());
        builder.withBatchTime(recorder.batchTime());
        builder.withBatchPoolSize(recorder.threadPoolSize());
        if (recorder.tenantId() != null) {
            builder.withTenantId(recorder.tenantId());
        }

        if (recorder.httpTracePublisher() != null) {
            builder.withTracePublisher(build(recorder.httpTracePublisher()));
        }

        return builder.build();
    }

    public TracePublisher build(HTTPTracePublisher publisher) {
        return new TracePublisherRESTClient(publisher.userName(), publisher.password(), publisher.url());
    }
}
