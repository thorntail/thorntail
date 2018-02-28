package org.jboss.unimbus.opentracing.impl;

import java.lang.reflect.Field;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import org.jboss.logging.Logger;

/**
 * @author Pavol Loffay
 */
@ApplicationScoped
public class TracerProducer {
    private static final Logger logger = Logger.getLogger(TracerProducer.class);

    /**
     * Resolves tracer instance to be used. It is using {@link TracerResolver} service loader to find
     * the tracer. It tracer is not resolved it will use {@link io.opentracing.NoopTracer}.
     *
     * @return tracer instance
     */
    @Produces
    @Default
    @Singleton
    public Tracer tracer() {
        Tracer tracer = TracerResolver.resolveTracer();
        if (tracer == null) {
            OpenTracingMessages.MESSAGES.noValidTracer();
            tracer = NoopTracerFactory.create();
        }

        OpenTracingMessages.MESSAGES.registeredTracer(tracer.getClass().getName());
        GlobalTracer.register(tracer);
        return tracer;
    }

    void dispose(@Disposes Tracer tracer) throws NoSuchFieldException, IllegalAccessException {
        Field f = GlobalTracer.class.getDeclaredField("tracer");
        f.setAccessible(true);
        f.set(null, NoopTracerFactory.create());
    }
}
