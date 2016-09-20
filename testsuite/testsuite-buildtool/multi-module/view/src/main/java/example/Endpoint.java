package example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Heiko Braun
 * @since 27/09/16
 */
@Path("/")
public class Endpoint {

    private Sample sample = new Sample();

    @GET
    public String speak() {
        return sample.saySomething();
    }
}
