package org.wildfly.swarm.opentracing.hawkular.jaxrs.filters;

import javax.naming.NamingException;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;
import org.wildfly.swarm.opentracing.hawkular.jaxrs.TracerLookup;

import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;


/**
 * @author Pavol Loffay
 */
@Provider
public class TracingDynamicFeature implements DynamicFeature {

    private static final Logger log = Logger.getLogger(TracingDynamicFeature.class);

    private ServerTracingDynamicFeature delegate;

    public TracingDynamicFeature() {
        log.info("OpenTracing Hawkular JAX-RS Dynamic Feature");
        try {
            ServerTracingDynamicFeature.Builder builder = TracerLookup.lookup().get();
            this.delegate = builder.build();
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup tracer builder", e);
        }
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        delegate.configure(resourceInfo, context);
    }
}
