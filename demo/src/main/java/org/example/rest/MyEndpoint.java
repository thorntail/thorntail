package org.example.rest;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.vertx.core.Vertx;
import io.vertx.resourceadapter.VertxConnectionFactory;

@Path("/my")
@Stateless
public class MyEndpoint {

    private static final String JNDI_NAME = "java:/eis/VertxConnectionFactory";

    @Resource(mappedName = JNDI_NAME)
    VertxConnectionFactory connectionFactory;

    @GET
    @Produces("text/plain")
    public Response doGet() throws Exception {
        Object obj = new InitialContext().lookup(JNDI_NAME);
        System.out.println(connectionFactory instanceof VertxConnectionFactory);
        System.out.println("VERTX: "+Vertx.class.getClassLoader());
        System.out.println("VCF: "+VertxConnectionFactory.class.getClassLoader());
        return Response.ok("Lookup: " + obj.getClass().getClassLoader()).build();
    }
}