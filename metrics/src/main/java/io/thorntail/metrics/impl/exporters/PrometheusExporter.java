/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.thorntail.metrics.impl.exporters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.thorntail.metrics.impl.MetricRegistries;
import io.thorntail.metrics.impl.jmx.Tag;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Metered;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Snapshot;
import org.eclipse.microprofile.metrics.Timer;
import org.jboss.logging.Logger;

/**
 * Export data in Prometheus text format
 *
 * @author Heiko W. Rupp
 */
@ApplicationScoped
public class PrometheusExporter implements Exporter {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

    private static final String LF = "\n";

    private static final String GAUGE = "gauge";

    private static final String SPACE = " ";

    private static final String SUMMARY = "summary";

    private static final String USCORE = "_";

    private static final String COUNTER = "counter";

    private static final String QUANTILE = "quantile";

    public StringBuffer exportOneScope(MetricRegistry.Type scope) {

        StringBuffer sb = new StringBuffer();
        getEntriesForScope(scope, sb);

        return sb;
    }

    @Override
    public StringBuffer exportAllScopes() {
        StringBuffer sb = new StringBuffer();

        for (MetricRegistry.Type scope : MetricRegistry.Type.values()) {
            getEntriesForScope(scope, sb);
        }

        return sb;
    }

    @Override
    public StringBuffer exportOneMetric(MetricRegistry.Type scope, String metricName) {
        MetricRegistry registry = this.registries.get(scope);
        Map<String, Metric> metricMap = registry.getMetrics();

        Metric m = metricMap.get(metricName);

        Map<String, Metric> outMap = new HashMap<>(1);
        outMap.put(metricName, m);

        StringBuffer sb = new StringBuffer();
        exposeEntries(scope, sb, registry, outMap);
        return sb;
    }


    @Override
    public String getContentType() {
        return "text/plain";
    }

    private void getEntriesForScope(MetricRegistry.Type scope, StringBuffer sb) {
        MetricRegistry registry = this.registries.get(scope);
        Map<String, Metric> metricMap = registry.getMetrics();

        exposeEntries(scope, sb, registry, metricMap);
    }

    private void exposeEntries(MetricRegistry.Type scope, StringBuffer sb, MetricRegistry registry, Map<String, Metric> metricMap) {
        for (Map.Entry<String, Metric> entry : metricMap.entrySet()) {
            String key = entry.getKey();
            Metadata md = registry.getMetadata().get(key);


            Metric metric = entry.getValue();

            switch (md.getTypeRaw()) {
                case GAUGE:
                case COUNTER:
                    key = getPrometheusMetricName(md, key);
                    String suffix = null;
                    if (!md.getUnit().equals(MetricUnits.NONE)) {
                        suffix = "_" + PrometheusUnit.getBaseUnitAsPrometheusString(md.getUnit());
                    }
                    writeTypeLine(sb, scope, key, md, suffix, null);
                    createSimpleValueLine(sb, scope, key, md, metric);
                    break;
                case METERED:
                    Metered meter = (Metered) metric;
                    writeMeterValues(sb, scope, meter, md);
                    break;
                case TIMER:
                    Timer timer = (Timer) metric;
                    writeTimerValues(sb, scope, timer, md);
                    break;
                case HISTOGRAM:
                    Histogram histogram = (Histogram) metric;
                    writeHistogramValues(sb, scope, histogram, md);
                    break;
                default:
                    throw new IllegalArgumentException("Not supported: " + key);

            }
        }
    }

    private void writeTimerValues(StringBuffer sb, MetricRegistry.Type scope, Timer timer, Metadata md) {

        String unit = md.getUnit();
        unit = PrometheusUnit.getBaseUnitAsPrometheusString(unit);

        String theUnit = unit.equals("none") ? "" : USCORE + unit;

        writeMeterRateValues(sb, scope, timer, md);
        Snapshot snapshot = timer.getSnapshot();
        writeSnapshotBasics(sb, scope, md, snapshot, theUnit);

        String suffix = USCORE + PrometheusUnit.getBaseUnitAsPrometheusString(md.getUnit());
        writeTypeLine(sb, scope, md.getName(), md, suffix, SUMMARY);
        writeValueLine(sb, scope, suffix + "_count", timer.getCount(), md);

        writeSnapshotQuantiles(sb, scope, md, snapshot, theUnit);
    }

    private void writeHistogramValues(StringBuffer sb, MetricRegistry.Type scope, Histogram histogram, Metadata md) {

        Snapshot snapshot = histogram.getSnapshot();
        String unit = md.getUnit();
        unit = PrometheusUnit.getBaseUnitAsPrometheusString(unit);

        String theUnit = unit.equals("none") ? "" : USCORE + unit;

        writeSnapshotBasics(sb, scope, md, snapshot, theUnit);
        writeTypeLine(sb, scope, md.getName(), md, theUnit, SUMMARY);
        writeValueLine(sb, scope, theUnit + "_count", histogram.getCount(), md);
        writeSnapshotQuantiles(sb, scope, md, snapshot, theUnit);
    }


    private void writeSnapshotBasics(StringBuffer sb, MetricRegistry.Type scope, Metadata md, Snapshot snapshot, String unit) {

        writeTypeAndValue(sb, scope, "_min" + unit, snapshot.getMin(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_max" + unit, snapshot.getMax(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_mean" + unit, snapshot.getMean(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_stddev" + unit, snapshot.getStdDev(), GAUGE, md);
    }

    private void writeSnapshotQuantiles(StringBuffer sb, MetricRegistry.Type scope, Metadata md, Snapshot snapshot, String unit) {
        writeValueLine(sb, scope, unit, snapshot.getMedian(), md, new Tag(QUANTILE, "0.5"));
        writeValueLine(sb, scope, unit, snapshot.get75thPercentile(), md, new Tag(QUANTILE, "0.75"));
        writeValueLine(sb, scope, unit, snapshot.get95thPercentile(), md, new Tag(QUANTILE, "0.95"));
        writeValueLine(sb, scope, unit, snapshot.get98thPercentile(), md, new Tag(QUANTILE, "0.98"));
        writeValueLine(sb, scope, unit, snapshot.get99thPercentile(), md, new Tag(QUANTILE, "0.99"));
        writeValueLine(sb, scope, unit, snapshot.get999thPercentile(), md, new Tag(QUANTILE, "0.999"));
    }

    private void writeMeterValues(StringBuffer sb, MetricRegistry.Type scope, Metered metric, Metadata md) {
        writeTypeAndValue(sb, scope, "_total", metric.getCount(), COUNTER, md);
        writeMeterRateValues(sb, scope, metric, md);
    }

    private void writeMeterRateValues(StringBuffer sb, MetricRegistry.Type scope, Metered metric, Metadata md) {
        writeTypeAndValue(sb, scope, "_rate_per_second", metric.getMeanRate(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_one_min_rate_per_second", metric.getOneMinuteRate(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_five_min_rate_per_second", metric.getFiveMinuteRate(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_fifteen_min_rate_per_second", metric.getFifteenMinuteRate(), GAUGE, md);
    }

    private void writeTypeAndValue(StringBuffer sb, MetricRegistry.Type scope, String suffix, double valueRaw, String type, Metadata md) {
        String key = md.getName();
        writeTypeLine(sb, scope, key, md, suffix, type);
        writeValueLine(sb, scope, suffix, valueRaw, md);
    }

    private void writeValueLine(StringBuffer sb, MetricRegistry.Type scope, String suffix, double valueRaw, Metadata md) {
        writeValueLine(sb, scope, suffix, valueRaw, md, null);
    }

    private void writeValueLine(StringBuffer sb, MetricRegistry.Type scope, String suffix, double valueRaw, Metadata md, Tag extraTag) {
        String name = md.getName();
        name = getPrometheusMetricName(md, name);
        fillBaseName(sb, scope, name);
        if (suffix != null) {
            sb.append(suffix);
        }
        // add tags

        Map<String, String> tags = new HashMap<>(md.getTags());
        if (extraTag != null) {
            tags.put(extraTag.getKey(), extraTag.getValue());
        }
        if (!tags.isEmpty()) {
            addTags(sb, tags);
        }

        sb.append(SPACE);
        sb.append(PrometheusUnit.scaleToBase(md.getUnit(), valueRaw)).append(LF);

    }

    private void addTags(StringBuffer sb, Map<String, String> tags) {
        Iterator<Map.Entry<String, String>> iter = tags.entrySet().iterator();
        sb.append("{");
        while (iter.hasNext()) {
            Map.Entry<String, String> tag = iter.next();
            sb.append(tag.getKey()).append("=\"").append(tag.getValue()).append("\"");
            if (iter.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("}");
    }

    private void fillBaseName(StringBuffer sb, MetricRegistry.Type scope, String key) {
        sb.append(scope.getName().toLowerCase()).append(":").append(key);
    }

    private void writeTypeLine(StringBuffer sb, MetricRegistry.Type scope, String key, Metadata md, String suffix, String typeOverride) {
        sb.append("# TYPE ");
        sb.append(scope.getName().toLowerCase());
        sb.append(':').append(getPrometheusMetricName(md, key));
        if (suffix != null) {
            sb.append(suffix);
        }
        sb.append(SPACE);
        if (typeOverride != null) {
            sb.append(typeOverride);
        } else if (md.getTypeRaw().equals(MetricType.TIMER)) {
            sb.append(SUMMARY);
        } else if (md.getTypeRaw().equals(MetricType.METERED)) {
            sb.append(COUNTER);
        } else {
            sb.append(md.getType());
        }
        sb.append("\n");
    }

    private void createSimpleValueLine(StringBuffer sb, MetricRegistry.Type scope, String key, Metadata md, Metric metric) {

        // value line
        fillBaseName(sb, scope, key);
        if (!md.getUnit().equals(MetricUnits.NONE)) {
            String unit = PrometheusUnit.getBaseUnitAsPrometheusString(md.getUnit());
            sb.append("_").append(unit);
        }
        String tags = md.getTagsAsString();
        if (tags != null && !tags.isEmpty()) {
            sb.append('{').append(tags).append('}');
        }

        Double valIn;
        if (md.getTypeRaw().equals(MetricType.GAUGE)) {
            Number value1 = (Number) ((Gauge) metric).getValue();
            if (value1 != null) {
                valIn = value1.doubleValue();
            } else {
                LOG.warn("Value is null for " + key);
                throw new IllegalStateException("Value must not be null for " + key);
            }
        } else {
            valIn = (double) ((Counter) metric).getCount();
        }

        Double value = PrometheusUnit.scaleToBase(md.getUnit(), valIn);
        sb.append(SPACE).append(value).append("\n");
    }


    private String getPrometheusMetricName(Metadata entry, String name) {
        String out = name.replace('-', '_').replace('.', '_').replace(' ', '_');
        out = decamelize(out);
        if (entry == null) {
            throw new IllegalStateException("No entry for " + name + " found");
        }
        out = out.replace("__", USCORE);
        out = out.replace(":_", ":");

        if (!out.matches(VALID_NAME)) {
            System.err.println("INVALID NAME: " + out);
        }

        return out;
    }

    private String decamelize(String in) {
        return in.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    private static String VALID_NAME = "^[a-zA-Z][_a-zA-Z0-9]*$";


    @Inject
    private MetricRegistries registries;
}
