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
@Produces("text/plain")
@Consumes("text/plain")
@ApplicationScoped
public class MyResource {

    @Inject
    private Jose jose;

    @POST
    @Path("sign")
    public String echoJws(String signedData) {
        return jose.sign(jose.verify(signedData));
    }

    @POST
    @Path("encrypt")
    public String echoJwe(String encryptedData) {
        return jose.encrypt(jose.decrypt(encryptedData));
    }

    @POST
    @Path("signAndEncrypt")
    public String echoJwsJwe(String signedAndEncryptedData) {
        return jose.encrypt(jose.sign(jose.verify(jose.decrypt(signedAndEncryptedData))));
    }
}
