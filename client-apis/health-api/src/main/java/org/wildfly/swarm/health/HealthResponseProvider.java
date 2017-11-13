package org.wildfly.swarm.health;

import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.spi.HealthCheckResponseProvider;

/**
 * Created by hbraun on 07.07.17.
 */
public class HealthResponseProvider implements HealthCheckResponseProvider {
    @Override
    public HealthCheckResponseBuilder createResponseBuilder() {
        return new BuilderImpl();
    }
}
