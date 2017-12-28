package org.wildfly.swarm.howto.autodetect;

import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class MyResource {

    @GET
    public String get() throws NamingException {
        return "Hello, Uberjar!";
    }
}
