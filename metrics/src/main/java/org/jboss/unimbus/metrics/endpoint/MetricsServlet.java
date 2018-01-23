package org.jboss.unimbus.metrics.endpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

/**
 * Created by bob on 1/22/18.
 */
public class MetricsServlet extends HttpServlet {

    private static final Map<String, ?> JSON_CONFIG = new HashMap<String, Object>() {{
        put(JsonGenerator.PRETTY_PRINTING, true);
    }};

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            getRoot(req, resp);
            return;
        }

        String[] parts = partsOf(path);
        String scope = null;
        String metricName = null;
        if (parts.length >= 1) {
            scope = parts[0];
        }
        if (parts.length == 2) {
            metricName = parts[1];
        }

        MetricRegistry registry = null;

        switch (scope) {
            case "application":
                registry = this.applicationRegistry;
                break;
            case "vendor":
                registry = this.vendorRegistry;
                break;
            case "base":
                registry = this.baseRegistry;
                break;
        }

        System.err.println("registry: " + registry);

        if (registry == null) {
            resp.sendError(404);
            return;
        }

        if (metricName == null) {
            send(registryJSON(registry), resp);
        }

        Metric metric = registry.getMetrics().get(metricName);
        if (metric == null) {
            resp.sendError(404);
            return;
        }

        JsonObjectBuilder json = Json.createObjectBuilder();
        metricJSON(json, metricName, metric);
        send(json.build(), resp);
    }

    String[] partsOf(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String[] parts = path.split("/");

        for (String part : parts) {
            System.err.println("one: [" + part + "]");
        }


        return parts;
    }

    private void send(JsonObject object, HttpServletResponse resp) throws IOException {
        OutputStream out = resp.getOutputStream();
        JsonWriterFactory factory = Json.createWriterFactory(JSON_CONFIG);
        try (JsonWriter writer = factory.createWriter(out)) {
            writer.writeObject(object);
        }
    }

    private void getRoot(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        send(rootJSON(), resp);
    }

    private JsonObject rootJSON() {
        JsonObjectBuilder root = Json.createObjectBuilder();

        root.add("base", registryJSON(this.baseRegistry));
        root.add("vendor", registryJSON(this.vendorRegistry));
        root.add("application", registryJSON(this.applicationRegistry));

        return root.build();
    }

    private JsonObject registryJSON(MetricRegistry registry) {
        JsonObjectBuilder registryJSON = Json.createObjectBuilder();
        Map<String, Metric> metrics = registry.getMetrics();
        metrics.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> {
                    metricJSON(registryJSON, e.getKey(), e.getValue());
                });
        return registryJSON.build();
    }

    private void metricJSON(JsonObjectBuilder registryJSON, String name, Metric metric) {
        if (metric instanceof Counter) {
            registryJSON.add(name, metricJSON((Counter) metric));
        } else if (metric instanceof Gauge) {
            Object value = ((Gauge) metric).getValue();
            if (value instanceof Long) {
                registryJSON.add(name, (long) value);
            } else if (value instanceof Integer) {
                registryJSON.add(name, (int) value);
            } else if (value instanceof Double) {
                registryJSON.add(name, (double) value);
            } else if (value instanceof Float) {
                registryJSON.add(name, (float) value);
            }
        }
    }

    private long metricJSON(Counter counter) {
        return counter.getCount();
    }

    private Object metricJSON(Gauge gauge) {
        return gauge.getValue();
    }

    @Inject
    @RegistryType(type = MetricRegistry.Type.BASE)
    MetricRegistry baseRegistry;

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    MetricRegistry vendorRegistry;

    @Inject
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    MetricRegistry applicationRegistry;

}
