package org.wildfly.swarm.cdi.jaxrsapi.test;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

/**
 * @author Ken Finnigan
 */
@Path("/messages")
@ApplicationScoped
public class MessageResource {

    public static final String MESSAGE_PREFIX = "The date and time is ";

    @Inject
    TimeService timeService;

    @GET
    @Path("/sync")
    public String getMessageSync2Sync() throws Exception {
        String time = timeService.getTime();
        if (time != null) {
            return message(time);
        } else {
            return "Time service unavailable";
        }
    }

    @GET
    @Path("/async")
    public void getMessageAsync2Sync(@Suspended final AsyncResponse asyncResponse) throws Exception {
        timeService.exec(() -> timeService.getTime(),
                         s -> asyncResponse.resume(this.message(s)),
                         asyncResponse::resume);
    }

    @GET
    @Path("/asyncZone")
    public void getMessageAsync2AsyncOffset(@Suspended final AsyncResponse asyncResponse, @QueryParam("zoneId") String zoneId) throws Exception {
        timeService.exec(() -> timeService.getTimeForZone(zoneId),
                         s -> asyncResponse.resume(this.message(s)),
                         asyncResponse::resume);
    }

    @GET
    @Path("/timeMessage")
    public void getTimeMessage(@Suspended final AsyncResponse asyncResponse) throws Exception {
        timeService.chainableExec(timeService::getTime, asyncResponse::resume)
                .thenApply((s) -> timeService.addMessage(s))
                .thenAccept(asyncResponse::resume)
                .exceptionally(t -> {
                    asyncResponse.resume(t);
                    return null;
                });
    }

    @GET
    @Path("/hello/{name}")
    public String hello(@PathParam("name") String name) throws Exception {
        return timeService.hello(name);
    }

    private String message(String time) {
        System.err.println("Time received is: " + time);
        return MESSAGE_PREFIX + time;
    }
}
