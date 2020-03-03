package org.wildfly.swarm.microprofile.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Readiness
public class TestDownReadinessHC implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.down(TestDownReadinessHC.class.getSimpleName());
    }
}
