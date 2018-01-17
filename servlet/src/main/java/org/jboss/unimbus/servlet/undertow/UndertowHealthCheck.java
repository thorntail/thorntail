package org.jboss.unimbus.servlet.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.undertow.Undertow;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Created by bob on 1/16/18.
 */
@Health
@ApplicationScoped
public class UndertowHealthCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder response = HealthCheckResponse.named("undertow");
        if ( ! this.undertow.getWorker().isShutdown() ) {
            response.up();
        }
        return response.build();
    }

    @Inject
    Undertow undertow;
}
