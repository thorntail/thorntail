package org.wildfly.swarm.keycloak;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.wildfly.swarm.keycloak.deployment.KeycloakSecurityContextAssociation;

@Path("/secured")
@Produces("text/plain")
public class SecuredResource {

    @GET
    public String get() {
        return "Hi " + KeycloakSecurityContextAssociation.get().getToken().getPreferredUsername()
            +  ", this resource is secured";
    }

    @GET
    @Path("sub")
    public String getSub() {
        return get();
    }

}
