/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.monitor.deployment;

import org.eclipse.microprofile.health.HealthCheckProcedure;
import org.eclipse.microprofile.health.HealthStatus;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 */
@ApplicationScoped
@Path("/health-delegate")
public class HttpDelegate {


    @Inject
    @Any
    Instance<HealthCheckProcedure> procedures;

    @GET
    @Produces(value = "application/json")
    public Response checkHealth() {

        if (procedures == null) {
            return Response.ok().status(204).build();
        }

        List<org.eclipse.microprofile.health.HealthStatus> responses = new ArrayList<>();

        for (org.eclipse.microprofile.health.HealthCheckProcedure procedure : procedures) {
            org.eclipse.microprofile.health.HealthStatus status = procedure.perform();
            responses.add(status);
        }

        StringBuffer sb = new StringBuffer("{");
        sb.append("\"checks\": [\n");

        int i = 0;
        boolean failed = false;

        for (org.eclipse.microprofile.health.HealthStatus resp : responses) {

            sb.append(toJson(resp));

            if (!failed) {
                failed = resp.getState() != HealthStatus.State.UP;
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

    private String toJson(HealthStatus status) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(QUOTE).append(ID).append("\":\"").append(status.getName()).append(QUOTE);
        sb.append(QUOTE).append(RESULT).append("\":\"").append(status.getState().name()).append(QUOTE);
        if (status.getAttributes().isPresent()) {
            sb.append(",");
            sb.append(QUOTE).append(DATA).append("\": {");
            Map<String, Object> atts = status.getAttributes().get();
            int i = 0;
            for (String key : atts.keySet()) {
                sb.append(QUOTE).append(key).append("\":").append(encode(atts.get(key)));
                if (i < atts.keySet().size() - 1) {
                    sb.append(",");
                }
                i++;
            }
            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    private String encode(Object o) {
        String res = null;
        if (o instanceof String) {
            res = "\"" + o.toString() + "\"";
        } else {
            res = o.toString();
        }

        return res;
    }

    private static final String ID = "id";

    private static final String RESULT = "result";

    private static final String DATA = "data";

    public static final String QUOTE = "\"";
}

