package org.jboss.unimbus.health;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * Created by bob on 1/16/18.
 */
@Health
@ApplicationScoped
public class BasicHealth implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("basic")
                .up()
                .build();
    }
}
