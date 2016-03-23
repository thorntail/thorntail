package org.wildfly.swarm.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Heiko Braun
 */
@Path("/another-app")
public class TimeResource {


    @GET
    @Path("/time")
    public String checkHealth() {
        return String.valueOf("Time:" + System.currentTimeMillis());
    }
}
