package org.jboss.unimbus.health;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.undertow.server.handlers.PathHandler;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.jboss.unimbus.events.LifecycleEvent;

/**
 * Created by bob on 1/16/18.
 */
@ApplicationScoped
public class HealthRegistry {

    void initialize(@Observes LifecycleEvent.Scan event) {
        root.addPrefixPath("/health", new HealthHandler(this.healthChecks));
    }

    @Inject
    PathHandler root;

    @Inject
    @Any
    @Health
    Instance<HealthCheck> healthChecks;
}
