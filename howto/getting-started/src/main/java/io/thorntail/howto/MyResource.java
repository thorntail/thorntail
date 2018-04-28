package io.thorntail.howto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Ken Finnigan
 */
@Path("/")
public class MyResource {
    @GET
    public String hello() {
        return "Hello World";
    }
}
