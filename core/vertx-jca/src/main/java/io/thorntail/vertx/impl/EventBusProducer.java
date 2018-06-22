package io.thorntail.vertx.impl;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.resource.ResourceException;

import io.thorntail.TraceMode;
import io.thorntail.vertx.impl.opentracing.TracedEventBus;
import io.vertx.resourceadapter.VertxConnection;
import io.vertx.resourceadapter.VertxEventBus;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 2/12/18.
 */
@Dependent
public class EventBusProducer {

    @Produces
    VertxEventBus eventBus() throws ResourceException {
        if ( this.trace.isPresent() && this.trace.get() != TraceMode.OFF ) {
            if ( isTracingAvailable() ) {
                VertxMessages.MESSAGES.tracingEnabled();
                return new TracedEventBus(this.connection.vertxEventBus());
            } else {
                VertxMessages.MESSAGES.tracingNotAvailable();
            }
        }
        return this.connection.vertxEventBus();
    }

    protected boolean isTracingAvailable() {
        try {
            Class<?> cls = Class.forName("io.opentracing.util.GlobalTracer");
            return true;
        } catch (ClassNotFoundException e) {
            // ignore;
        }

        return false;
    }

    @Inject
    VertxConnection connection;

    @Inject
    @ConfigProperty(name="vertx.trace")
    Optional<TraceMode> trace;
}
