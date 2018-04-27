package io.thorntail.jaxrs.impl.opentracing.jaxrs;

import java.util.concurrent.Executors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import io.opentracing.Tracer;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * {@link javax.ws.rs.client.ClientBuilder} with installed tracing components.
 *
 * @author Pavol Loffay
 */
public class TracedClientBuilder extends ResteasyClientBuilder {

    private final Tracer tracer;

    public TracedClientBuilder() {
        Instance<Tracer> tracerInstance = CDI.current().select(Tracer.class);
        this.tracer = tracerInstance.get();
        super.register(new ClientTracingFeature.Builder(tracer).build());
    }

    @Override
    public ResteasyClient build() {
        if (asyncExecutor == null) {
            cleanupExecutor = true;
            asyncExecutor = Executors.newFixedThreadPool(10);
        }
        this.asyncExecutor = new TracedExecutorService(asyncExecutor, tracer);
        return super.build();
    }
}
