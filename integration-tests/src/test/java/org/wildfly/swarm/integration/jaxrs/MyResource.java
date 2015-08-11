package org.wildfly.swarm.integration.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.joda.time.DateTime;

/**
 * @author Bob McWhirter
 */
@Path("/")
public class MyResource {

    @GET
    @Produces("text/plain")
    public String get() {
        return "Howdy at " + new DateTime();
    }
}
