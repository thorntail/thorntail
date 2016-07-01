package org.wildfly.swarm.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author Bob McWhirter
 */
@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    @Override
    public Response toResponse(JsonProcessingException e) {
        return Response.ok( "mapped json: " + e.getMessage() ).build();
    }
}
