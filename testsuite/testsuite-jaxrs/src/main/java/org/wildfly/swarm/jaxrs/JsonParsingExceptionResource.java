package org.wildfly.swarm.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;

/**
 * @author Bob McWhirter
 */
@Path("/json")
public class JsonParsingExceptionResource {

    @GET
    @Path("/throw")
    public Response get() throws JsonParseException {
        throw new JsonParseException("that didn't work", new JsonLocation("synthetic.json", 0, 0, 0));
    }
}
