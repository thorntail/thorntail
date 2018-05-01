package org.wildfly.swarm.jaxrs;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
    public String echoJws(String jws) {
        return jose.sign(jose.verify(jws));
    }

    @POST
    @Path("signDetached")
    public Response echoJwsDetached(@HeaderParam("DetachedData") String detachedData, String jws) {
        String data = jose.verifyDetached(jws, detachedData);
        return Response.ok(jose.sign(data)).header("DetachedData", data).build();
    }

    @POST
    @Path("encrypt")
    public String echoJwe(String jwe) {
        return jose.encrypt(jose.decrypt(jwe));
    }

    @POST
    @Path("signAndEncrypt")
    public String echoJwsJwe(String signedAndEncryptedData) {
        return jose.encrypt(jose.sign(jose.verify(jose.decrypt(signedAndEncryptedData))));
    }
}
