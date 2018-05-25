package org.wildfly.swarm.mvc.test;

import javax.mvc.annotation.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/hello")
@Controller
public class MvcController  {

    @GET
    public String greet() {
        return "hello.jsp";
    }
}
