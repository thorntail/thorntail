package org.example.rest;

import javax.annotation.Resource;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import io.vertx.core.Vertx;
import io.vertx.resourceadapter.VertxConnectionFactory;

@Path("/my")
public class MyEndpoint {
	@Resource(name = "java:/eis/VertxConnectionFactory")
	VertxConnectionFactory connectionFactory;

	@GET
	@Produces("text/plain")
	public Response doGet() {
		return Response.ok("Lookup: "+connectionFactory).build();
	}
}