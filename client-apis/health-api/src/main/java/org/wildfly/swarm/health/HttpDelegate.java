package org.wildfly.swarm.health;


import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


import org.eclipse.microprofile.health.Status;

/**
 * @author Heiko Braun
 */
@ApplicationScoped
@Path("/health")
public class HttpDelegate {

    @Inject
    @Any
    Instance<org.eclipse.microprofile.health.HealthCheckProcedure> procedures;

    @GET
    @Produces(value = "application/json")
    public Response checkHealth() {

        if (null == procedures) {
            return Response.ok().status(204).build();
        }

        List<org.eclipse.microprofile.health.HealthStatus> responses = new ArrayList<>();

        for (org.eclipse.microprofile.health.HealthCheckProcedure procedure : procedures) {
            org.eclipse.microprofile.health.HealthStatus status = procedure.execute();
            responses.add(status);
        }

        StringBuffer sb = new StringBuffer("{");
        sb.append("\"checks\": [\n");

        int i = 0;
        boolean failed = false;

        for (org.eclipse.microprofile.health.HealthStatus resp : responses) {

            sb.append(resp.toJson());

            if (!failed) {
                failed = resp.getState() != Status.State.UP;
            }

            if (i < responses.size() - 1) {
                sb.append(",\n");
            }
            i++;
        }
        sb.append("],\n");

        String outcome = failed ? "DOWN" : "UP";
        sb.append("\"outcome\": \"" + outcome + "\"\n");
        sb.append("}\n");

        return Response.ok(sb.toString()).build();
    }

}
