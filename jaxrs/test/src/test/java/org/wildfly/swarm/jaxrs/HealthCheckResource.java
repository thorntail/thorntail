package org.wildfly.swarm.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.wildfly.swarm.monitor.Health;
import org.wildfly.swarm.monitor.HealthStatus;

/**
 * @author Heiko Braun
 */
@Path("/app")
public class HealthCheckResource {


    @GET
    @Health
    @Path("/health-secure")
    public HealthStatus checkHealth() {
        return HealthStatus.up().withAttribute("status", "UP");
    }

    @GET
    @Health(inheritSecurity = false)
    @Path("/health-insecure")
    public HealthStatus checkHealthInsecure() {
        return HealthStatus.up().withAttribute("status", "UP");
    }
}
