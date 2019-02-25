package org.wildfly.swarm.microprofile.jwtauth.keycloak;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/secured")
@Produces("text/plain")
public class SecuredResource {

    @Inject
    private JsonWebToken jwt;

    @GET
    @Path("/")
    @RolesAllowed("admin")
    public String get() {
        return "Hi " + jwt.getName() + ", this resource is secured";
    }

}
