package io.thorntail.tracing.impl;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.util.GlobalTracer;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.impl.KernelMessages;

/**
 * @author Pavol Loffay
 */
@ApplicationScoped
public class TracerProducer {

    private static Tracer INSTANCE;

    void init(@Observes LifecycleEvent.Bootstrap event) {
        //tracer();
    }

    @PostConstruct
    void init() {
        if ( INSTANCE != null ) {
            return;
        }
        INSTANCE = TracerResolver.resolveTracer();
        if (INSTANCE == null) {
            KernelMessages.MESSAGES.noValidTracer();
            INSTANCE = NoopTracerFactory.create();
        }

        KernelMessages.MESSAGES.registeredTracer(INSTANCE.getClass().getName());
        GlobalTracer.register(INSTANCE);
    }

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
        return INSTANCE;
    }

    void dispose(@Disposes Tracer tracer) throws NoSuchFieldException, IllegalAccessException {
        Field f = GlobalTracer.class.getDeclaredField("tracer");
        f.setAccessible(true);
        f.set(null, NoopTracerFactory.create());
        INSTANCE = null;
    }

}
