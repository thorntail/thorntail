package org.wildfly.swarm.cdi.jaxrsapi.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.wildfly.swarm.client.jaxrs.Service;
import org.wildfly.swarm.client.jaxrs.ServiceClient;

/**
 * @author Ken Finnigan
 */
@Path("time")
@Service(baseUrl = "http://localhost:8080/")
public interface TimeService extends ServiceClient<TimeService> {
    @GET
    @Path("default")
    @Produces(MediaType.TEXT_PLAIN)
    String getTime();

    @GET
    @Path("tz")
    @Produces(MediaType.TEXT_PLAIN)
    String getTimeForZone(@QueryParam("zoneId") String zoneId);

    @GET
    @Path("message")
    @Produces(MediaType.TEXT_PLAIN)
    String addMessage(@QueryParam("time") String time);

    @GET
    @Path("hello/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    String hello(@PathParam("name") String name);
}
