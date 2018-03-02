package org.jboss.unimbus.jaxrs.impl.opentracing.jaxrs;

import java.io.IOException;
import java.util.Collections;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.client.ClientSpanDecorator;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFilter;

@Produces
public class ClientTracingFilterWrapper implements ClientRequestFilter, ClientResponseFilter {

    private ClientTracingFilter tracingFilter;

    public ClientTracingFilterWrapper() {
        try {
            Instance<Tracer> tracerInstance = CDI.current().select(Tracer.class);
            tracingFilter = new ClientTracingFilter(tracerInstance.get(),
                                                    Collections.singletonList(ClientSpanDecorator.STANDARD_TAGS));
        } catch (IllegalStateException ex) {
            //skip - in TCK this fails
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (tracingFilter != null) {
            tracingFilter.filter(requestContext);
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        if (tracingFilter != null) {
            tracingFilter.filter(requestContext, responseContext);
        }
    }
}

