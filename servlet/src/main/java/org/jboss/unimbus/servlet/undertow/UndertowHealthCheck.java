package org.jboss.unimbus.servlet.undertow;

import io.undertow.Undertow;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Created by bob on 1/16/18.
 */
//@Health
//@ApplicationScoped
public class UndertowHealthCheck implements HealthCheck {

    UndertowHealthCheck(String name, Undertow undertow) {
        this.name = name;
        this.undertow = undertow;
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder response = HealthCheckResponse.named(name);
        if (!this.undertow.getWorker().isShutdown()) {
            response.up();
        }
        return response.build();
    }


    private String name;

    private Undertow undertow;

}
