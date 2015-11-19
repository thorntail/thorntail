package org.wildfly.swarm.examples.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.joda.time.DateTime;

/**
 * @author Bob McWhirter
 */
@Path("/")
public class MyResource {

    @GET
    @Produces("text/plain")
    public String get() {
        // Prove we can use an external dependency and weird JDK classes.
        return "Howdy at " + new DateTime() + ".  Have a JDK class: " + javax.security.auth.login.LoginException.class.getName();
    }
}
