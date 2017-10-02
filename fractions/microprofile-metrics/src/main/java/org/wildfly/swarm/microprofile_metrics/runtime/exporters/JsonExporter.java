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
import org.eclipse.microprofile.metrics.Snapshot;
import org.wildfly.swarm.microprofile_metrics.runtime.MetricRegistryFactory;
import org.wildfly.swarm.microprofile_metrics.runtime.app.HistogramImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.MeterImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.TimerImpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author hrupp
 */
public class JsonExporter implements Exporter {

  private static final String COMMA_LF = ",\n";
  private static final String LF = "\n";

  @Override
  public StringBuilder exportOneScope(MetricRegistry.Type scope) {

    StringBuilder sb = new StringBuilder();

    getMetricsForAScope(sb,scope);

    return sb;
  }

  private void getMetricsForAScope(StringBuilder sb, MetricRegistry.Type scope) {

    MetricRegistry registry = MetricRegistryFactory.get(scope);
    Map<String,Metric> metricMap = registry.getMetrics();
    Map<String,Metadata> metadataMap = registry.getMetadata();

    sb.append("{\n");

    writeMetricsForMap(sb, metricMap, metadataMap);

    sb.append("}");
  }

  private void writeMetricsForMap(StringBuilder sb, Map<String, Metric> metricMap, Map<String,Metadata> metadataMap) {

    for (Iterator<Map.Entry<String, Metric>> iterator = metricMap.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry<String, Metric> entry = iterator.next();
      String key = entry.getKey();

      Metric value = entry.getValue();
      Metadata metadata = metadataMap.get(key);

      switch (metadata.getTypeRaw()) {
        case GAUGE:
        case COUNTER:
          Number val = getValueFromMetric(value, key);
          sb.append("  ").append('"').append(key).append('"').append(" : ").append(val);
          break;
        case METERED:
          MeterImpl meter = (MeterImpl) value;
          writeStartLine(sb, key);
          writeMeterValues(sb, meter);
          writeEndLine(sb);
          break;
        case TIMER:
          TimerImpl timer = (TimerImpl) value;
          writeStartLine(sb, key);
          writeTimerValues(sb, timer);
          writeEndLine(sb);
          break;
        case HISTOGRAM:
          HistogramImpl hist = (HistogramImpl) value;
          writeStartLine(sb, key);
          sb.append("    \"count\": ").append(hist.getCount()).append(COMMA_LF);
          writeSnapshotValues(sb,hist.getSnapshot());
          writeEndLine(sb);
          break;
        default:
          System.err.println("JSE, Not yet supported: " + metadata);

      }
      if (iterator.hasNext()) {
        sb.append(',');
      }
      sb.append(LF);
    }
  }

  private void writeEndLine(StringBuilder sb) {
    sb.append("  }");
  }

  private void writeStartLine(StringBuilder sb, String key) {
    sb.append("  ").append('"').append(key).append('"').append(" : ").append("{\n");
  }

  private void writeMeterValues(StringBuilder sb, Metered meter) {
    sb.append("    \"count\": ").append(meter.getCount()).append(COMMA_LF);
    sb.append("    \"meanRate\": ").append(meter.getMeanRate()).append(COMMA_LF);
    sb.append("    \"oneMinRate\": ").append(meter.getOneMinuteRate()).append(COMMA_LF);
    sb.append("    \"fiveMinRate\": ").append(meter.getFiveMinuteRate()).append(COMMA_LF);
    sb.append("    \"fifteenMinRate\": ").append(meter.getFifteenMinuteRate()).append(LF);
  }

  private void writeTimerValues(StringBuilder sb, TimerImpl timer) {
    writeSnapshotValues(sb,timer.getSnapshot());
    // Backup and write COMMA_LF
    sb.setLength(sb.length() - 1);
    sb.append(COMMA_LF);
    writeMeterValues(sb, timer.getMeter());
  }

  private void writeSnapshotValues(StringBuilder sb, Snapshot snapshot) {
    sb.append("    \"p50\": ").append(snapshot.getMedian()).append(COMMA_LF);
    sb.append("    \"p75\": ").append(snapshot.get75thPercentile()).append(COMMA_LF);
    sb.append("    \"p95\": ").append(snapshot.get95thPercentile()).append(COMMA_LF);
    sb.append("    \"p98\": ").append(snapshot.get98thPercentile()).append(COMMA_LF);
    sb.append("    \"p99\": ").append(snapshot.get99thPercentile()).append(COMMA_LF);
    sb.append("    \"p999\": ").append(snapshot.get999thPercentile()).append(COMMA_LF);
    sb.append("    \"min\": ").append(snapshot.getMin()).append(COMMA_LF);
    sb.append("    \"mean\": ").append(snapshot.getMean()).append(COMMA_LF);
    sb.append("    \"max\": ").append(snapshot.getMax()).append(COMMA_LF);
    // Can't be COMMA_LF has there may not be anything following as is the case for a Histogram
    sb.append("    \"stddev\": ").append(snapshot.getStdDev()).append(LF);

  }


  private Number getValueFromMetric(Metric theMetric, String name) {
    if (theMetric instanceof Gauge) {
      Number value = (Number) ((Gauge) theMetric).getValue();
      double v;
      if (value != null) {
        return value;
      } else {
        System.out.println("Value is null for " + name);
        return -142.142; // TODO
      }
    } else if (theMetric instanceof Counter) {
      return ((Counter) theMetric).getCount();
    } else {
      System.err.println("Not yet supported metric: " + theMetric.getClass().getName());
      return -42.42;
    }
  }

  @Override
  public StringBuilder exportAllScopes() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");

    MetricRegistry.Type[] values = MetricRegistry.Type.values();
    int totalNonEmptyScopes = Helper.countNonEmptyScopes();

    int scopes = 0;
    for (int i = 0; i < values.length; i++) {
      MetricRegistry.Type scope = values[i];
      MetricRegistry registry = MetricRegistryFactory.get(scope);

      if (registry.getNames().size() > 0) {
        sb.append('"').append(scope.getName().toLowerCase()).append('"').append(" :\n");
        getMetricsForAScope(sb,scope);
        sb.append("\n");
        scopes++;
        if (scopes < totalNonEmptyScopes) {
          sb.append(',');
        }
      }
    }

    sb.append("}");
    return sb;
  }

  @Override
  public StringBuilder exportOneMetric(MetricRegistry.Type scope, String metricName) {
    MetricRegistry registry = MetricRegistryFactory.get(scope);
    Map<String,Metric> metricMap = registry.getMetrics();
    Map<String,Metadata> metadataMap = registry.getMetadata();


    Metric m = metricMap.get(metricName);

    Map<String,Metric> outMap = new HashMap<>(1);
    outMap.put(metricName,m);

    StringBuilder sb = new StringBuilder();
    sb.append("{");
    writeMetricsForMap(sb,outMap, metadataMap);
    sb.append("\n");

    return sb;
  }

  @Override
  public String getContentType() {
    return "application/json";
  }
}
