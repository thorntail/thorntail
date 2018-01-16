package org.jboss.unimbus.health;

import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * Created by bob on 1/16/18.
 */
public class HealthHandler implements HttpHandler {
    public HealthHandler(Iterable<HealthCheck> checks) {
        this.checks = checks;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        //exchange.startBlocking();
        if ( exchange.isInIoThread()) {
            exchange.dispatch(Executors.newSingleThreadExecutor(), this);
            return;
        }
        exchange.startBlocking();
        OutputStream out = exchange.getOutputStream();
        JsonWriter writer = Json.createWriter(out);
        writer.writeObject(jsonObject());
        writer.close();
    }

    private JsonObject jsonObject() {
        JsonArrayBuilder results = Json.createArrayBuilder();
        HealthCheckResponse.State outcome = HealthCheckResponse.State.UP;

        for (HealthCheck check : checks) {
            JsonObject each = jsonObject(check);
            results.add(each);
            if ( outcome == HealthCheckResponse.State.UP ) {
                String state = each.getString("state");
                if ( state.equals("DOWN" ) ) {
                    outcome = HealthCheckResponse.State.DOWN;
                }
            }
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();


        builder.add( "outcome", outcome.toString() );
        builder.add( "checks", results );

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
        });

        return builder.build();
    }

    private final Iterable<HealthCheck> checks;
}
