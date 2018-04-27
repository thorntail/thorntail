package io.thorntail.testsuite.jaxrs.jsonp;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
    @Produces("application/json")
    public JsonObject get() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( "tacos", 42 );
        return builder.build();
    }
}
