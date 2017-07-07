package org.wildfly.swarm.monitor.runtime;

import org.eclipse.microprofile.health.ResponseBuilder;
import org.eclipse.microprofile.health.spi.SPIFactory;

/**
 * Created by hbraun on 07.07.17.
 */
public class HealthFactory implements SPIFactory {
    @Override
    public ResponseBuilder createResponseBuilder() {
        return new BuilderImpl();
    }
}
