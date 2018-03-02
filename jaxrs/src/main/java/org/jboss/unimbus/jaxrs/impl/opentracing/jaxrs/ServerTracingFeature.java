package org.jboss.unimbus.jaxrs.impl.opentracing.jaxrs;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.server.OperationNameProvider;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature.Builder;


/**
 * Created by bob on 2/19/18.
 */
@ApplicationScoped
@Provider
public class ServerTracingFeature implements DynamicFeature {

    private ServerTracingDynamicFeature delegate;

    public ServerTracingFeature() {
        Instance<Tracer> tracerInstance = CDI.current().select(Tracer.class);
        this.delegate = new Builder(tracerInstance.get())
                .withOperationNameProvider(OperationNameProvider.ClassNameOperationName.newBuilder())
                .withTraceSerialization(false)
                .build();
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        delegate.configure(resourceInfo, context);
    }
}
