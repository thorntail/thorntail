package org.jboss.unimbus.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by bob on 1/15/18.
 */
@Path("/")
public class ProofResource {

    @GET
    @Path("/")
    public String get() {
        return "Hello!";
    }
}
