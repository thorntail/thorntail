package org.wildfly.swarm.jaxrs;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.wildfly.swarm.jose.Jose;

/**
 *
 */
@Path("/")
@ApplicationScoped
public class MyResource {

    @Inject
    private Jose jose;

    @POST
    @Produces("text/plain")
    @Consumes("text/plain")
    @Path("sign")
    public String echoJws(String signedData) {
        return jose.sign(jose.verify(signedData));
    }

    @POST
    @Produces("text/plain")
    @Consumes("text/plain")
    @Path("encrypt")
    public String echoJwe(String encryptedData) {
        return jose.encrypt(jose.decrypt(encryptedData));
    }
}