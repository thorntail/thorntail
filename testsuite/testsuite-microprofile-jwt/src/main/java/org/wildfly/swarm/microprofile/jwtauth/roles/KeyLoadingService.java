package org.wildfly.swarm.microprofile.jwtauth.roles;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/signer-key")
public class KeyLoadingService {
    @GET
    public InputStream getKeyStream() {

        return KeyLoadingService.class.getClassLoader().getResourceAsStream("/META-INF/MP-JWT-SIGNER");
    }
}
