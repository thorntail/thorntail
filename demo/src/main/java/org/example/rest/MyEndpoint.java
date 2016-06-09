package org.example.rest;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.vertx.core.Vertx;

@Path("/my")
@Stateless
public class MyEndpoint {

    //java.lang.ClassCastException: io.vertx.resourceadapter.impl.VertxConnectionFactoryImpl cannot be cast to io.vertx.resourceadapter.VertxConnectionFactory
//   @Resource(mappedName = "java:/eis/VertxConnectionFactory")
//	VertxConnectionFactory connectionFactory;

    @GET
    @Produces("text/plain")
    public Response doGet() throws Exception {
        //FIXME: java.lang.ClassCastException: io.vertx.resourceadapter.impl.VertxConnectionFactoryImpl cannot be cast to io.vertx.resourceadapter.VertxConnectionFactory
        Object connectionFactory = new InitialContext().lookup("java:/eis/VertxConnectionFactory");
        System.out.println(Vertx.vertx());
        return Response.ok("Lookup: " + connectionFactory).build();
    }
}