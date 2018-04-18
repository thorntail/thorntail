package io.thorntail.health.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * Created by bob on 1/17/18.
 */
public class HealthServlet extends HttpServlet {

    private static final Map<String, ?> JSON_CONFIG = new HashMap<String, Object>() {{
        put(JsonGenerator.PRETTY_PRINTING, true);
    }};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        OutputStream out = resp.getOutputStream();
        JsonWriterFactory factory = Json.createWriterFactory(JSON_CONFIG);
        JsonWriter writer = factory.createWriter(out);

        JsonObject payload = jsonObject();
        String outcome = payload.getString("outcome");
        if ( outcome.equals(HealthCheckResponse.State.DOWN.toString())) {
            resp.setStatus(503);
        }

        writer.writeObject(payload);
        writer.close();
    }

    private JsonObject jsonObject() {
        JsonArrayBuilder results = Json.createArrayBuilder();
        HealthCheckResponse.State outcome = HealthCheckResponse.State.UP;

        for (HealthCheck check : checks) {
            if (check == null) {
                continue;
            }
            JsonObject each = jsonObject(check);
            results.add(each);
            if (outcome == HealthCheckResponse.State.UP) {
                String state = each.getString("state");
                if (state.equals("DOWN")) {
                    outcome = HealthCheckResponse.State.DOWN;
                }
            }
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();


        builder.add("outcome", outcome.toString());
        builder.add("checks", results);

        return builder.build();
    }

    private JsonObject jsonObject(HealthCheck check) {
        return jsonObject(check.call());
    }

    private JsonObject jsonObject(HealthCheckResponse response) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("name", response.getName());
        builder.add("state", response.getState().toString());
        response.getData().ifPresent(d -> {
            JsonObjectBuilder data = Json.createObjectBuilder();
            for (Map.Entry<String, Object> entry : d.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    data.add(entry.getKey(), (String) value);
                } else if (value instanceof Long) {
                    data.add(entry.getKey(), (Long) value);
                } else if (value instanceof Boolean) {
                    data.add(entry.getKey(), (Boolean) value);
                }
            }
            builder.add("data", data.build());
        });

        return builder.build();
    }

    @Inject
    @Health
    private Instance<HealthCheck> checks;
}


