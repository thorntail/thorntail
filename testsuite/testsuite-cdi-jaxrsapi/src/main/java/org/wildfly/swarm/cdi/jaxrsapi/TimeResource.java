package org.wildfly.swarm.cdi.jaxrsapi;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * @author Ken Finnigan
 */
@Path("/time")
public class TimeResource {
    public final static String INTRO_MESSAGE = "Howdy at ";

    public static final String MESSAGE_HELLO = "Hello to ";

    @GET
    @Path("/default")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTime() {
        return ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    @GET
    @Path("/tz")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTimeForZone(@QueryParam("zoneId") String zoneId) {
        return ZonedDateTime.of(LocalDateTime.now(), ZoneId.of(zoneId)).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    @GET
    @Path("/message")
    @Produces(MediaType.TEXT_PLAIN)
    public String addMessage(@QueryParam("time") String time) {
        return INTRO_MESSAGE + time;
    }

    @GET
    @Path("/hello/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@PathParam("name") String name) throws Exception {
        return MESSAGE_HELLO + name;
    }
}
