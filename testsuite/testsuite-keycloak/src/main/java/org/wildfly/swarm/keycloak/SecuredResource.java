package org.wildfly.swarm.keycloak;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.wildfly.swarm.keycloak.deployment.KeycloakSecurityContextAssociation;

@Path("/secured")
public class SecuredResource {

    @GET
    @Produces("text/plain")
    public String get() {
        return "Hi " + KeycloakSecurityContextAssociation.get().getToken().getPreferredUsername()
            +  ", this resource is secured";
    }
}
