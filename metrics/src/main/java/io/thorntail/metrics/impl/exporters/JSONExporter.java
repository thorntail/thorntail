package io.thorntail.metrics.impl.exporters;

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
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Snapshot;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
public class JSONExporter implements Exporter {

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

        Metric metric = registry.getMetrics().get(metricName);

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

        if ( ! this.baseRegistry.getMetrics().isEmpty() ) {
            root.add("base", registryJSON(this.baseRegistry));
        }
        if ( ! this.vendorRegistry.getMetrics().isEmpty() ) {
            root.add("vendor", registryJSON(this.vendorRegistry));
        }
        if ( ! this.applicationRegistry.getMetrics().isEmpty() ) {
            root.add("application", registryJSON(this.applicationRegistry));
        }

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
        } else if (metric instanceof Meter) {
            registryJSON.add(name, metricJSON((Meter) metric));
        } else if (metric instanceof Timer) {
            registryJSON.add(name, metricJSON((Timer) metric));
        } else if (metric instanceof Histogram) {
            registryJSON.add(name, metricJSON((Histogram) metric));
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

    private JsonObject metricJSON(Meter meter) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add( "count", meter.getCount() );
        builder.add( "meanRate", meter.getMeanRate() );
        builder.add( "oneMinRate", meter.getOneMinuteRate() );
        builder.add( "fiveMinRate", meter.getFiveMinuteRate() );
        builder.add( "fifteenMinRate", meter.getFifteenMinuteRate() );

        return builder.build();
    }

    private Object metricJSON(Gauge gauge) {
        return gauge.getValue();
    }

    private JsonObject metricJSON(Histogram histogram) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        Snapshot snap = histogram.getSnapshot();

        builder.add("count", histogram.getCount());
        builder.add("min", snap.getMin());
        builder.add("max", snap.getMax());
        builder.add("mean", snap.getMean());
        builder.add("stddev", snap.getStdDev());
        builder.add("p50", snap.getMedian());
        builder.add("p75", snap.get75thPercentile());
        builder.add("p95", snap.get95thPercentile());
        builder.add("p98", snap.get98thPercentile());
        builder.add("p99", snap.get99thPercentile());
        builder.add("p999", snap.get999thPercentile());

        return builder.build();
    }

    private JsonObject metricJSON(Timer timer) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        Snapshot snap = timer.getSnapshot();

        builder.add("meanRate", timer.getMeanRate());
        builder.add("oneMinRate", timer.getOneMinuteRate());
        builder.add("fiveMinRate", timer.getFiveMinuteRate());
        builder.add("fifteenMinRate", timer.getFifteenMinuteRate());

        builder.add("count", timer.getCount());
        builder.add("min", snap.getMin());
        builder.add("max", snap.getMax());
        builder.add("mean", snap.getMean());
        builder.add("stddev", snap.getStdDev());
        builder.add("p50", snap.getMedian());
        builder.add("p75", snap.get75thPercentile());
        builder.add("p95", snap.get95thPercentile());
        builder.add("p98", snap.get98thPercentile());
        builder.add("p99", snap.get99thPercentile());
        builder.add("p999", snap.get999thPercentile());

        return builder.build();

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
