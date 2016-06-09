package org.example.rest;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.vertx.core.Vertx;
import io.vertx.resourceadapter.VertxConnectionFactory;

@Path("/my")
@Stateless
public class MyEndpoint {

    @Resource(mappedName = "java:/eis/VertxConnectionFactory")
    VertxConnectionFactory connectionFactory;

    @GET
    @Produces("text/plain")
    public Response doGet() throws Exception {
        System.out.println(Vertx.vertx());
        return Response.ok("Lookup: " + connectionFactory).build();
    }
}