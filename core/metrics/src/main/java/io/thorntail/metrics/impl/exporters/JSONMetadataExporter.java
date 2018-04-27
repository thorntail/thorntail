package io.thorntail.metrics.impl.exporters;

import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
public class JSONMetadataExporter implements Exporter {

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
        if (registry == null) {
            return null;
        }

        JsonObject obj = registryJSON(registry);
        return stringify(obj);
    }

    @Override
    public StringBuffer exportAllScopes() {
        JsonObject obj = rootJSON();
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
        if (registry == null) {
            return null;
        }

        Metadata metric = registry.getMetadata().get(metricName);

        if (metric == null) {
            return null;
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();
        metricJSON(builder, metricName, metric);
        return stringify(builder.build());
    }

    private static final Map<String, ?> JSON_CONFIG = new HashMap<String, Object>() {{
        put(JsonGenerator.PRETTY_PRINTING, true);
    }};

    StringBuffer stringify(JsonObject obj) {
        StringWriter out = new StringWriter();
        try (JsonWriter writer = Json.createWriterFactory(JSON_CONFIG).createWriter(out)) {
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
        Map<String, Metadata> metrics = registry.getMetadata();
        metrics.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> {
                    metricJSON(registryJSON, e.getKey(), e.getValue());
                });
        return registryJSON.build();
    }

    private void metricJSON(JsonObjectBuilder registryJSON, String name, Metadata metric) {
        registryJSON.add(name, metricJSON(metric));
    }

    private JsonObject metricJSON(Metadata metadata) {
        JsonObjectBuilder obj = Json.createObjectBuilder();

        if (metadata.getUnit() != null) {
            obj.add("unit", metadata.getUnit());
        }
        if (metadata.getType() != null) {
            obj.add("type", metadata.getType());
        }
        if (metadata.getDescription() != null) {
            obj.add("description", metadata.getDescription());
        }
        if (metadata.getDisplayName() != null) {
            obj.add("displayName", metadata.getDisplayName());
        }
        if (metadata.getTagsAsString() != null) {
            //obj.add("tags", metadata.getTagsAsString());
            String str = metadata.getTags().entrySet().stream()
                    .map(e-> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(","));
            obj.add( "tags", str );
        }

        return obj.build();
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
