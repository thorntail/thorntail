package io.thorntail.testsuite.security.keycloak;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Ken Finnigan
 */
@Path("/")
public class MyResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "Hello from JAX-RS";
    }
}
