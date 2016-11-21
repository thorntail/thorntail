package org.wildfly.swarm.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.wildfly.swarm.monitor.Health;
import org.wildfly.swarm.monitor.HealthStatus;

/**
 * @author Heiko Braun
 */
@Path("/failed")
public class FailedChecks {

    @GET
    @Health
    @Path("/first")
    public HealthStatus checkHealth() {
        return HealthStatus.named("first").down();
    }

    @GET
    @Health
    @Path("/second")
    public HealthStatus checkHealthInsecure() {
        return HealthStatus.named("second").up().withAttribute("time", System.currentTimeMillis());
    }
}
