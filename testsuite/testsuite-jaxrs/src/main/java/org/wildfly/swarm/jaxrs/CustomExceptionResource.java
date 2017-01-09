package org.wildfly.swarm.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author Bob McWhirter
 */
@Path("/custom")
public class CustomExceptionResource {

    @GET
    @Path("/throw")
    public Response get() throws CustomException {
        throw new CustomException("Custom exception");
    }

}
