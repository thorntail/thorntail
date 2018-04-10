package org.wildfly.swarm.microprofile.jwtauth;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("/token")
public class TokenResource {
    @Inject
    @Claim(standard = Claims.raw_token)
    private String rawToken;

    @GET
    public Response doGet() {
        return Response.ok(rawToken, MediaType.TEXT_PLAIN).build();
    }
}
