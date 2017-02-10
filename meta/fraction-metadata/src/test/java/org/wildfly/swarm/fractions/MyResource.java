package org.wildfly.swarm.fractions;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Ken Finnigan
 */
@Path("/")
public class MyResource {

    @GET
    @Produces("text/plain")
    public String test() {
        return "a string";
    }
}
