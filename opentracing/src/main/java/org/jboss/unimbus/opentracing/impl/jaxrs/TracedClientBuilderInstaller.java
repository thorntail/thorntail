package org.jboss.unimbus.opentracing.impl.jaxrs;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;

import org.jboss.unimbus.ServiceRegistry;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.opentracing.impl.OpenTracingMessages;

/**
 * Created by bob on 2/20/18.
 */
@RequiredClassPresent("javax.ws.rs.client.ClientBuilder")
@ApplicationScoped
public class TracedClientBuilderInstaller {

    void install(@Observes LifecycleEvent.Initialize event) {
        this.registry.register(ClientBuilder.class, TracedClientBuilder.class);
        OpenTracingMessages.MESSAGES.setUpJaxRsClient();
    }

    @Inject
    ServiceRegistry registry;
}
