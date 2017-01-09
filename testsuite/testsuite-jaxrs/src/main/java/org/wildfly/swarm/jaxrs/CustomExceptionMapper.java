package org.wildfly.swarm.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Bob McWhirter
 */
@Provider
public class CustomExceptionMapper implements ExceptionMapper<CustomException> {

    @Override
    public Response toResponse(CustomException e) {
        return Response.ok("mapped custom: " + e.getMessage()).build();
    }
}
