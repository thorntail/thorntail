package org.wildfly.swarm.integration.ejb;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Ken Finnigan
 */
@Stateless
@Path("/")
public class MyResource {

    @EJB
    private GreeterEJB greeter;

    @GET
    @Produces("text/plain")
    public String get() {
        return greeter.message();
    }
}
