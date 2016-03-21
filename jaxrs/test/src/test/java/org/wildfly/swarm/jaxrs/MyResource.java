package org.wildfly.swarm.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.wildfly.swarm.monitor.Health;

/**
 * @author Heiko Braun
 */
@Path("/app")
public class MyResource {

    @Health
    @GET
    @Produces("text/plain")
    @Path("/health")
    public String getName() {
        return "Howdy from " + MyResource.class.getName();
    }
}
