package org.wildfly.swarm.keycloak;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/secured")
public class SecuredResource {

    @GET
    @Produces("text/plain")
    public String get() {
        return "This resource is secured";
    }
}
