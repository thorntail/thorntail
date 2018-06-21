package io.thorntail.vertx.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;

import io.vertx.core.Vertx;
import io.vertx.resourceadapter.impl.VertxManagedConnectionFactory;
import io.vertx.resourceadapter.impl.VertxPlatformConfiguration;
import io.vertx.resourceadapter.impl.VertxPlatformFactory;

/**
 * Created by bob on 6/21/18.
 */
@ApplicationScoped
public class VertxProducer implements VertxPlatformFactory.VertxListener {

    @Override
    public void whenReady(Vertx vertx) {
        System.err.println( "vertx is ready: " + vertx);
        this.holder.set(vertx);
        this.latch.countDown();
    }

    @Produces
    Vertx vertx() throws NamingException, ResourceException, InterruptedException {
        this.holder.updateAndGet((v) -> {
            if (v == null) {
                VertxPlatformFactory.instance().getOrCreateVertx(this.vertxPlatformConfig, this);
            }

            return v;
        });

        this.latch.await();

        return this.holder.get();
    }

    @Inject
    InitialContext context;

    @Inject
    VertxPlatformConfiguration vertxPlatformConfig;

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Vertx> holder = new AtomicReference<>();

}
