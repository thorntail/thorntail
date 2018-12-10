package org.wildfly.swarm.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/jsonb")
@Produces("application/json")
@Consumes("application/json")
public class MyResource {


    @POST
    public Dog echoModified(Dog dog) {
        if (!dog.bitable) {
            dog.bitable = true;
        }
        return dog;
    }

}
