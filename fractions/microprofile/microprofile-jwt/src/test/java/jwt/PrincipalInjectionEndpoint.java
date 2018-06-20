package jwt;

import java.security.Principal;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Validate that the injection of a {@linkplain Principal} works when using the MP-JWT feature.
 * This validates that the MP-JWT implementation is not interfering with the CDI built in
 * Principal bean.
 */
@Path("/endp")
@RequestScoped
@RolesAllowed("Tester")
public class PrincipalInjectionEndpoint {
    @Inject
    private Principal principal;
    @Inject
    private JsonWebToken principal2;

    @GET
    @Path("/verifyInjectedPrincipal")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject verifyInjectedPrincipal() {
        boolean pass = false;
        String msg;
        if (principal == null) {
            msg = "principal value is null, FAIL";
        }
        else if (principal instanceof JsonWebToken) {
            msg = Claims.iss.name() + " PASS";
            pass = true;
        }
        else {
            msg = String.format("principal: JsonWebToken != %s", principal.getClass().getCanonicalName());
        }
        JsonObject result = Json.createObjectBuilder()
                .add("pass", pass)
                .add("msg", msg)
                .build();
        return result;
    }

}
