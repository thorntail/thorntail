package org.jboss.unimbus.metrics.exporters;

import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
public class JSONExporter implements Exporter  {

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public StringBuffer exportOneScope(MetricRegistry.Type scope) {
        MetricRegistry registry = null;
        switch (scope) {
            case APPLICATION:
                registry = this.applicationRegistry;
                break;
            case BASE:
                registry = this.baseRegistry;
                break;
            case VENDOR:
                registry = this.vendorRegistry;
                break;
        }
        if ( registry == null ) {
            return null;
        }

        JsonObject obj = registryJSON(registry);
        return stringify(obj);
    }

    @Override
    public StringBuffer exportAllScopes() {
        JsonObject obj =  rootJSON();
        return stringify(obj);
    }

    @Override
    public StringBuffer exportOneMetric(MetricRegistry.Type scope, String metricName) {
        MetricRegistry registry = null;
        switch (scope) {
            case APPLICATION:
                registry = this.applicationRegistry;
                break;
            case BASE:
                registry = this.baseRegistry;
                break;
            case VENDOR:
                registry = this.vendorRegistry;
                break;
        }
        if ( registry == null ) {
            return null;
        }

        Metric metric = registry.getMetrics().get(metricName);

        if ( metric == null ) {
            return null;
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();
        metricJSON( builder, metricName, metric );
        return stringify(builder.build());
    }

    private static final Map<String, ?> JSON_CONFIG = new HashMap<String, Object>() {{
        put(JsonGenerator.PRETTY_PRINTING, true);
    }};

    StringBuffer stringify(JsonObject obj) {
        StringWriter out = new StringWriter();
        try ( JsonWriter writer = Json.createWriterFactory(JSON_CONFIG).createWriter(out) ) {
            writer.writeObject(obj);
        }
        return out.getBuffer();
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
