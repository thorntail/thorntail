package io.thorntail.vertx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.jboss.weld.exceptions.IllegalStateException;

import io.thorntail.TraceMode;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.vertx.tracing.VertxEventBusInterceptor;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * Initializes a {@link Vertx} instance and triggers registration of various components.
 *
 * <p>
 * Configuration of the {@link Vertx} instance is performed using the built-in mechanisms. The list of supported property names is not defined. Instead,
 * properties with {@link VertxProperties#PROPERTY_PREFIX} prefix can be mapped to setter methods of the {@link VertxOptions} object. A property name consits of
 * the prefix and dot separated parts derived from the setter name. For example, the {@link VertxOptions#setWorkerPoolSize(int)} setter method is mapped to the
 * {@code vertx.worker.pool.size} property.
 * </p>
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class VertxInitializer {

    private TraceMode traceMode;

    private Vertx vertx;

    @Inject
    @Any
    private Instance<Object> instance;

    void init(@Observes LifecycleEvent.Bootstrap bootstrap, Config config, BeanManager beanManager) throws InstantiationException, IllegalAccessException {

        traceMode = VertxProperties.getTraceMode(config);
        VertxOptions options = VertxProperties.createOptions(VertxOptions.class, config, VertxProperties.PROPERTY_PREFIX);
        VertxLogger.LOG.usingOptions(options);

        if (options.isClustered()) {
            CountDownLatch latch = new CountDownLatch(1);
            Vertx.clusteredVertx(options, ar -> {
                try {
                    if (ar.succeeded()) {
                        vertx = ar.result();
                    } else {
                        throw new IllegalStateException("Could not create a clustered Vertx instance", ar.cause());
                    }
                } finally {
                    latch.countDown();
                }
            });
            try {
                if (!latch.await(15, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Could not create a clustered Vertx instance");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            VertxLogger.LOG.clusteredVertxInstanceCreated();

        } else {
            vertx = Vertx.vertx(options);
            VertxLogger.LOG.vertxInstanceCreated();
        }

        VertxExtension extension = beanManager.getExtension(VertxExtension.class);
        Event<Object> event = beanManager.getEvent();
        extension.registerComponents(vertx, event, beanManager);

        // Deploy Verticles registered as beans
        for (Verticle verticle : instance.select(Verticle.class)) {
            VertxLogger.LOG.deployVerticle(verticle.getClass().getName());
            vertx.deployVerticle(verticle);
        }

        // Intercept EventBus send operations if tracing is enabled
        if (!TraceMode.OFF.equals(traceMode)) {
            vertx.eventBus().addInterceptor(instance.select(VertxEventBusInterceptor.class).get());
        }

        // Make it possible to init dependent components, e.g. create an HTTP server
        event.select(Vertx.class).fire(vertx);
    }

    public TraceMode getTraceMode() {
        return traceMode;
    }

}
