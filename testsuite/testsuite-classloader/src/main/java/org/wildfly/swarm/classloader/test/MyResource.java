package org.wildfly.swarm.classloader.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Bob McWhirter
 */
@Path("/")
public class MyResource {

    @GET
    public String get() throws ClassNotFoundException {
        return "success: " + Class.forName("com.fasterxml.jackson.datatype.jdk8.Jdk8BeanSerializerModifier").getName();
    }
}
