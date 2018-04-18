package org.wildfly.swarm.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.wildfly.swarm.jose.JoseException;

@Provider
public class JoseExceptionMapper implements ExceptionMapper<JoseException> {

    @Override
    public Response toResponse(JoseException exception) {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

}
