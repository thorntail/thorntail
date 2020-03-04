package org.wildfly.swarm.microprofile.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Liveness
public class TestUpLivenessHC implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up(TestUpLivenessHC.class.getSimpleName());
    }
}
