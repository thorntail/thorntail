package org.wildfly.swarm.opentracing.hawkular.jaxrs.runtime;

import javax.enterprise.inject.Vetoed;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.swarm.opentracing.hawkular.jaxrs.TracerLookup;

import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;


/**
 * @author Pavol Loffay
 */
@Vetoed
public class OpenTracingHawkularJaxRsService implements Service<OpenTracingHawkularJaxRsService>, TracerLookup {

    private static final Logger log = Logger.getLogger(OpenTracingHawkularJaxRsService.class);

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm", "opentracing", "hawkular", "jaxrs");

    private ServerTracingDynamicFeature.Builder builder;

    public OpenTracingHawkularJaxRsService(ServerTracingDynamicFeature.Builder builder) {
        this.builder = builder;
    }

    @Override
    public void start(StartContext context) throws StartException {
        log.info("OpenTracing Hawkular JAX-RS service started");
    }

    @Override
    public void stop(StopContext context) {
        log.info("OpenTracing Hawkular JAX-RS service stopped");
    }

    @Override
    public OpenTracingHawkularJaxRsService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public ServerTracingDynamicFeature.Builder get() {
        return builder;
    }
}
