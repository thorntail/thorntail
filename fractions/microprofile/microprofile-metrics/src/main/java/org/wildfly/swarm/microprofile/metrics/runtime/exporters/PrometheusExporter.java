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

package org.wildfly.swarm.microprofile.metrics.runtime.exporters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Metered;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Snapshot;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.metrics.runtime.MetricRegistries;
import org.wildfly.swarm.microprofile.metrics.runtime.Tag;
import org.wildfly.swarm.microprofile.metrics.runtime.app.HistogramImpl;
import org.wildfly.swarm.microprofile.metrics.runtime.app.MeterImpl;
import org.wildfly.swarm.microprofile.metrics.runtime.app.TimerImpl;

/**
 * Export data in Prometheus text format
 *
 * @author Heiko W. Rupp
 */
public class PrometheusExporter implements Exporter {

    // This allows to suppress the (noisy) # HELP line
    private static final String SWARM_MICROPROFILE_METRICS_OMIT_HELP_LINE = "swarm.microprofile.metrics.omitHelpLine";

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

    private static final String LF = "\n";
    private static final String GAUGE = "gauge";
    private static final String SPACE = " ";
    private static final String SUMMARY = "summary";
    private static final String USCORE = "_";
    private static final String COUNTER = "counter";
    private static final String QUANTILE = "quantile";

    private boolean writeHelpLine;

    public PrometheusExporter() {
        Config config = ConfigProvider.getConfig();
        Optional<Boolean> tmp = config.getOptionalValue(SWARM_MICROPROFILE_METRICS_OMIT_HELP_LINE, Boolean.class);
        writeHelpLine = !tmp.isPresent() || !tmp.get();
    }

    public StringBuilder exportOneScope(MetricRegistry.Type scope) {

        StringBuilder sb = new StringBuilder();
        getEntriesForScope(scope, sb);

        return sb;
    }

    @Override
    public StringBuilder exportAllScopes() {
        StringBuilder sb = new StringBuilder();

        for (MetricRegistry.Type scope : MetricRegistry.Type.values()) {
            getEntriesForScope(scope, sb);
        }

        return sb;
    }

    @Override
    public StringBuilder exportOneMetric(MetricRegistry.Type scope, String metricName) {
        MetricRegistry registry = MetricRegistries.get(scope);
        Map<String, Metric> metricMap = registry.getMetrics();

        Metric m = metricMap.get(metricName);

        Map<String, Metric> outMap = new HashMap<>(1);
        outMap.put(metricName, m);

        StringBuilder sb = new StringBuilder();
        exposeEntries(scope, sb, registry, outMap);
        return sb;
    }


    @Override
    public String getContentType() {
        return "text/plain";
    }

    private void getEntriesForScope(MetricRegistry.Type scope, StringBuilder sb) {
        MetricRegistry registry = MetricRegistries.get(scope);
        Map<String, Metric> metricMap = registry.getMetrics();

        exposeEntries(scope, sb, registry, metricMap);
    }

    private void exposeEntries(MetricRegistry.Type scope, StringBuilder sb, MetricRegistry registry, Map<String, Metric> metricMap) {
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
                        suffix = USCORE + PrometheusUnit.getBaseUnitAsPrometheusString(md.getUnit());
                    }
                    writeHelpLine(sb, scope, key, md, suffix);
                    writeTypeLine(sb, scope, key, md, suffix, null);
                    createSimpleValueLine(sb, scope, key, md, metric);
                    break;
                case METERED:
                    MeterImpl meter = (MeterImpl) metric;
                    writeMeterValues(sb, scope, meter, md);
                    break;
                case TIMER:
                    TimerImpl timer = (TimerImpl) metric;
                    writeTimerValues(sb, scope, timer, md);
                    break;
                case HISTOGRAM:
                    HistogramImpl histogram = (HistogramImpl) metric;
                    writeHistogramValues(sb, scope, histogram, md);
                    break;
                default:
                    throw new IllegalArgumentException("Not supported: " + key);

            }
        }
    }

    private void writeTimerValues(StringBuilder sb, MetricRegistry.Type scope, TimerImpl timer, Metadata md) {

        String unit = md.getUnit();
        unit = PrometheusUnit.getBaseUnitAsPrometheusString(unit);

        String theUnit = unit.equals("none") ? "" : USCORE + unit;

        writeMeterRateValues(sb, scope, timer.getMeter(), md);
        Snapshot snapshot = timer.getSnapshot();
        writeSnapshotBasics(sb, scope, md, snapshot, theUnit);

        String suffix = USCORE + PrometheusUnit.getBaseUnitAsPrometheusString(md.getUnit());
        writeHelpLine(sb, scope, md.getName(), md, suffix);
        writeTypeLine(sb,scope,md.getName(),md, suffix,SUMMARY);
        writeValueLine(sb,scope,suffix + "_count",timer.getCount(),md, null, false);

        writeSnapshotQuantiles(sb, scope, md, snapshot, theUnit);
    }

    private void writeHistogramValues(StringBuilder sb, MetricRegistry.Type scope, HistogramImpl histogram, Metadata md) {

        Snapshot snapshot = histogram.getSnapshot();
        String unit = md.getUnit();
        unit = PrometheusUnit.getBaseUnitAsPrometheusString(unit);

        String theUnit = unit.equals("none") ? "" : USCORE + unit;

        writeHelpLine(sb, scope, md.getName(), md, SUMMARY);
        writeSnapshotBasics(sb, scope, md, snapshot, theUnit);
        writeTypeLine(sb,scope,md.getName(),md, theUnit,SUMMARY);
        writeValueLine(sb,scope,theUnit + "_count",histogram.getCount(),md, null, false);
        writeSnapshotQuantiles(sb, scope, md, snapshot, theUnit);
    }


    private void writeSnapshotBasics(StringBuilder sb, MetricRegistry.Type scope, Metadata md, Snapshot snapshot, String unit) {

        writeTypeAndValue(sb, scope, "_min" + unit, snapshot.getMin(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_max" + unit, snapshot.getMax(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_mean" + unit, snapshot.getMean(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_stddev" + unit, snapshot.getStdDev(), GAUGE, md);
    }

    private void writeSnapshotQuantiles(StringBuilder sb, MetricRegistry.Type scope, Metadata md, Snapshot snapshot, String unit) {
        writeValueLine(sb, scope, unit, snapshot.getMedian(), md, new Tag(QUANTILE, "0.5"));
        writeValueLine(sb, scope, unit, snapshot.get75thPercentile(), md, new Tag(QUANTILE, "0.75"));
        writeValueLine(sb, scope, unit, snapshot.get95thPercentile(), md, new Tag(QUANTILE, "0.95"));
        writeValueLine(sb, scope, unit, snapshot.get98thPercentile(), md, new Tag(QUANTILE, "0.98"));
        writeValueLine(sb, scope, unit, snapshot.get99thPercentile(), md, new Tag(QUANTILE, "0.99"));
        writeValueLine(sb, scope, unit, snapshot.get999thPercentile(), md, new Tag(QUANTILE, "0.999"));
    }

    private void writeMeterValues(StringBuilder sb, MetricRegistry.Type scope, Metered metric, Metadata md) {
        writeHelpLine(sb, scope, md.getName(), md, "_total");
        writeTypeAndValue(sb, scope, "_total", metric.getCount(), COUNTER, md);
        writeMeterRateValues(sb, scope, metric, md);
    }

    private void writeMeterRateValues(StringBuilder sb, MetricRegistry.Type scope, Metered metric, Metadata md) {
        writeTypeAndValue(sb, scope, "_rate_per_second", metric.getMeanRate(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_one_min_rate_per_second", metric.getOneMinuteRate(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_five_min_rate_per_second", metric.getFiveMinuteRate(), GAUGE, md);
        writeTypeAndValue(sb, scope, "_fifteen_min_rate_per_second", metric.getFifteenMinuteRate(), GAUGE, md);
    }

    private void writeTypeAndValue(StringBuilder sb, MetricRegistry.Type scope, String suffix, double valueRaw, String type, Metadata md) {
        String key = md.getName();
        writeTypeLine(sb, scope, key, md, suffix, type);
        writeValueLine(sb, scope, suffix, valueRaw, md);
    }

    private void writeValueLine(StringBuilder sb, MetricRegistry.Type scope, String suffix, double valueRaw, Metadata md) {
        writeValueLine(sb, scope, suffix, valueRaw, md, null);
    }

    private void writeValueLine(StringBuilder sb, MetricRegistry.Type scope, String suffix, double valueRaw, Metadata md, Tag extraTag) {
        writeValueLine(sb, scope, suffix, valueRaw, md, extraTag, true);
    }

    private void writeValueLine(StringBuilder sb,
                                MetricRegistry.Type scope,
                                String suffix,
                                double valueRaw,
                                Metadata md,
                                Tag extraTag,
                                boolean scaled) {
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
        Double value = scaled ? PrometheusUnit.scaleToBase(md.getUnit(), valueRaw) : valueRaw;
        sb.append(value).append(LF);

    }

    private void addTags(StringBuilder sb, Map<String, String> tags) {
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

    private void fillBaseName(StringBuilder sb, MetricRegistry.Type scope, String key) {
        sb.append(scope.getName().toLowerCase()).append(":").append(key);
    }

    private void writeHelpLine(StringBuilder sb, MetricRegistry.Type scope, String key, Metadata md, String suffix) {
        // Only write this line if we actually have a description in metadata
        if (writeHelpLine && md.getDescription() != null) {
            sb.append("# HELP ");
            sb.append(scope.getName().toLowerCase());
            sb.append(':').append(getPrometheusMetricName(md, key));
            if (suffix != null) {
                sb.append(suffix);
            }
            sb.append(SPACE);
            sb.append(md.getDescription());
            sb.append(LF);
        }

    }

    private void writeTypeLine(StringBuilder sb, MetricRegistry.Type scope, String key, Metadata md, String suffix, String typeOverride) {
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
        sb.append(LF);
    }

    private void createSimpleValueLine(StringBuilder sb, MetricRegistry.Type scope, String key, Metadata md, Metric metric) {

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
        sb.append(SPACE).append(value).append(LF);

    }


    private String getPrometheusMetricName(Metadata entry, String name) {
        String out = name.replaceAll("[^\\w]+",USCORE);
        out = decamelize(out);
        if (entry == null) {
            throw new IllegalStateException("No entry for " + name + " found");
        }
        out = out.replace("__", USCORE);
        out = out.replace(":_", ":");

        return out;
    }

    private String decamelize(String in) {
        return in.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

}
