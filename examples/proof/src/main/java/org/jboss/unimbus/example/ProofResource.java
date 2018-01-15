package org.jboss.unimbus.example;

import javax.enterprise.context.ApplicationScoped;
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
        System.err.println( "GET GET GET");
        return "Hello!";
    }
}
