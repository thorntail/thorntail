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
package org.wildfly.swarm.microprofile_metrics.runtime.exporters;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Metered;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Snapshot;
import org.wildfly.swarm.microprofile_metrics.runtime.MetricRegistryFactory;
import org.wildfly.swarm.microprofile_metrics.runtime.app.MeterImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.TimerImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Export data in Prometheus text format
 * @author Heiko W. Rupp
 */
public class PrometheusExporter implements Exporter {

    public static final byte LF = '\n';
    private static final String SPACE_GAUGE = " gauge";

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
  public String getContentType() {
    return "text/plain";
  }

  private void getEntriesForScope(MetricRegistry.Type scope, StringBuilder sb) {
    MetricRegistry registry = MetricRegistryFactory.get(scope);
      Map<String,Metric> metricMap = registry.getMetrics();

    for (Map.Entry<String,Metric> entry: metricMap.entrySet()) {
      String key = entry.getKey();
      Metadata md = registry.getMetadata().get(key);

      key = getPrometheusMetricName(md,key);
        writeTypeLine(scope, sb, key, md);

        Metric metric = entry.getValue();

        switch (md.getTypeRaw()) {
          case GAUGE:
          case COUNTER:
              createSimpleValueLine(sb,scope,key,md, metric);
              break;
          case METERED:
              MeterImpl meter = (MeterImpl) metric;
              writeMeterValues(sb,scope, meter, md, key);
              break;
            case TIMER:
                TimerImpl timer = (TimerImpl) metric;
                writeTimerValues(sb,scope, timer, md, key);
                break;
            default:
              System.err.println("Not yet supported: " + key);
              continue;

      }
    }
  }

    private void writeTimerValues(StringBuilder sb, MetricRegistry.Type scope, TimerImpl timer, Metadata md, String key) {
        writeMeterValues(sb,scope,timer.getMeter(),md,key);
        Snapshot snapshot = timer.getSnapshot();
        fillBaseName(sb, scope, key).append("_min_seconds ").append(snapshot.getMin()).append(LF);
        fillBaseName(sb, scope, key).append("_max_seconds ").append(snapshot.getMax()).append(LF);
        fillBaseName(sb, scope, key).append("_mean_seconds ").append(snapshot.getMean()).append(LF);
        fillBaseName(sb, scope, key).append("_stddev_seconds ").append(snapshot.getStdDev()).append(LF);

/*

| `rate_per_second`               | Gauge   | `getMeanRate()`                     | PER_SECOND
| `one_min_rate_per_second`       | Gauge   | `getOneMinuteRate()`                | PER_SECOND
| `five_min_rate_per_second`      | Gauge   | `getFiveMinuteRate()`               | PER_SECOND
| `fifteen_min_rate_per_second`   | Gauge   | `getFifteenMinuteRate()`            | PER_SECOND
| `min_seconds`                   | Gauge   | `getSnapshot().getMin()`            | SECONDS^1^
| `max_seconds`                   | Gauge   | `getSnapshot().getMax()`            | SECONDS^1^
| `mean_seconds`                  | Gauge   | `getSnapshot().getMean()`           | SECONDS^1^
| `stddev_seconds`                | Gauge   | `getSnapshot().getStdDev()`         | SECONDS^1^
| `seconds_count`^2^              | Summary | `getCount()`                        | N/A
| `seconds{quantile="0.5"}`^2^    | Summary | `getSnapshot().getMedian()`         | SECONDS^1^
| `seconds{quantile="0.75"}`^2^   | Summary | `getSnapshot().get75thPercentile()` | SECONDS^1^
| `seconds{quantile="0.95"}`^2^   | Summary | `getSnapshot().get95thPercentile()` | SECONDS^1^
| `seconds{quantile="0.98"}`^2^   | Summary | `getSnapshot().get98thPercentile()` | SECONDS^1^
| `seconds{quantile="0.99"}`^2^   | Summary | `getSnapshot().get99thPercentile()` | SECONDS^1^
| `seconds{quantile="0.999"}`^2^  | Summary | `getSnapshot().get999thPercentile()`| SECONDS^1^
 */
// TODO scale values
// TODO is value always _per_second?
// TODO remaining values
    }

    private void writeMeterValues(StringBuilder sb, MetricRegistry.Type scope, Metered metric, Metadata md, String key) {
// TODO scale values
// TODO is value always _per_second?
        fillBaseName(sb, scope, key).append("_total ").append(metric.getCount()).append(LF);
        fillBaseName(sb, scope, key).append("_rate_per_second ").append(metric.getMeanRate()).append(SPACE_GAUGE).append(LF);
        fillBaseName(sb, scope, key).append("_one_min_rate_per_second ").append(metric.getOneMinuteRate()).append(SPACE_GAUGE).append(LF);
        fillBaseName(sb, scope, key).append("_five_min_rate_per_second ").append(metric.getFiveMinuteRate()).append(SPACE_GAUGE).append(LF);
        fillBaseName(sb, scope, key).append("_fifteen_min_rate_per_second ").append(metric.getFifteenMinuteRate()).append(SPACE_GAUGE).append(LF);
    }

    private StringBuilder fillBaseName(StringBuilder sb, MetricRegistry.Type scope, String key) {
        return sb.append(scope.getName().toLowerCase()).append(":").append(key);
    }

    private void writeTypeLine(MetricRegistry.Type scope, StringBuilder sb, String key, Metadata md) {
        sb.append("# TYPE ");
        sb.append(scope.getName().toLowerCase());
        sb.append(':').append(key).append(" ").append(md.getType()).append("\n");
    }

    private void createSimpleValueLine(StringBuilder sb, MetricRegistry.Type scope, String key, Metadata md, Metric metric) {

        // value line
        fillBaseName(sb, scope, key);
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
                valIn = -42.142;
                System.out.println("Value is null for " + key);
                //throw new IllegalStateException("Value must not be null for " + key); TODO enable later
            }
        } else {
            valIn = (double) ((Counter) metric).getCount();
        }

        Double value = PrometheusUnit.scaleToBase(md.getUnit(),valIn);
        sb.append(" ").append(value).append("\n");

    }

    @Override
    public StringBuilder exportOneMetric(MetricRegistry.Type scope, String metricName) {
        MetricRegistry registry = MetricRegistryFactory.get(scope);
        Map<String,Metric> metricMap = registry.getMetrics();

        Metric m = metricMap.get(metricName);
        Metadata metadata = registry.getMetadata().get(metricName);

        Map<String,Metric> outMap = new HashMap<>(1);
        outMap.put(metricName,m);

        String key = getPrometheusMetricName(metadata,metricName);

        StringBuilder sb = new StringBuilder();
        writeTypeLine(scope,sb,key,metadata);
        createSimpleValueLine(sb,scope,key,metadata,outMap.entrySet().iterator().next().getValue());
        return sb;
    }

    private String getPrometheusMetricName(Metadata entry, String name) {
      String out = name.replace('-', '_').replace('.', '_').replace(' ','_');
      out = decamelize(out);
      if (entry == null) {
        throw new IllegalStateException("No entry for " + name + " found");
      }
      if (entry.getUnit() == null) {
        throw new IllegalStateException("Entry " + entry + " has no unit ");
      }
      if (!entry.getUnit().equals(MetricUnits.NONE)) {
          out = out + "_" + PrometheusUnit.getBaseUnitAsPrometheusString(entry.getUnit());
      }
      out = out.replace("__","_");
      out = out.replace(":_",":");

      return out;
  }

  private String decamelize(String in) {
      return in.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
  }

}
