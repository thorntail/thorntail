package org.example.rest;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.vertx.core.Vertx;
import io.vertx.resourceadapter.VertxConnection;
import io.vertx.resourceadapter.VertxConnectionFactory;

@Path("/my")
@Stateless
public class MyEndpoint {

    @Resource(mappedName = "java:/eis/VertxConnectionFactory")
    VertxConnectionFactory connectionFactory;

    @GET
    @Produces("text/plain")
    public Response doGet() throws Exception {
        VertxConnection vertxConnection = connectionFactory.getVertxConnection();
        try {
            vertxConnection.vertxEventBus().send("tacos","A message");
        } finally {
            vertxConnection.close();
        }
        return Response.ok("OK!").build();
    }
}