package io.thorntail.jaxrs.impl.opentracing.jaxrs;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;

import io.thorntail.ServiceRegistry;
import io.thorntail.events.LifecycleEvent;

/**
 * Created by bob on 2/20/18.
 */
@ApplicationScoped
public class TracedClientBuilderInstaller {

    void install(@Observes LifecycleEvent.Initialize event) {
        this.registry.register(ClientBuilder.class, TracedClientBuilder.class);
    }

    @Inject
    ServiceRegistry registry;
}
