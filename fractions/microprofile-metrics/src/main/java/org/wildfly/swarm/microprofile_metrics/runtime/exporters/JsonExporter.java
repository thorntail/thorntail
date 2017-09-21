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
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.wildfly.swarm.microprofile_metrics.runtime.MetricRegistryFactory;

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
          Number val = getValueFromMetric(value);
          sb.append("  ").append('"').append(key).append('"').append(" : ").append(val);
          break;
        case METERED:
          Meter meter = (Meter) value;
          sb.append("  ").append('"').append(key).append('"').append(" : ").append("{\n");
          writeMeterValues(sb, meter);
          sb.append("  }");
          break;
        case TIMER:
          Timer timer = (Timer) value;
          sb.append("  ").append('"').append(key).append('"').append(" : ").append("{\n");
          writeTimerValues(sb, timer);
          sb.append("  }");


          break;
        case HISTOGRAM:
          default:
            System.err.println("Not yet supported " + metadata);

      }


      if (iterator.hasNext()) {
        sb.append(',');
      }
      sb.append(LF);
    }
  }

  private void writeMeterValues(StringBuilder sb, Meter meter) {
    sb.append("    \"count\": ").append(meter.getCount()).append(COMMA_LF);
    sb.append("    \"meanRate\": ").append(meter.getMeanRate()).append(COMMA_LF);
    sb.append("    \"oneMinRate\": ").append(meter.getOneMinuteRate()).append(COMMA_LF);
    sb.append("    \"fiveMinRate\": ").append(meter.getFiveMinuteRate()).append(COMMA_LF);
    sb.append("    \"fifteenMinRate\": ").append(meter.getFifteenMinuteRate()).append(LF);
  }

  private void writeTimerValues(StringBuilder sb, Timer timer) {
    sb.append("    \"count\": ").append(timer.getCount()).append(COMMA_LF);
    sb.append("    \"meanRate\": ").append(timer.getMeanRate()).append(COMMA_LF);
    sb.append("    \"oneMinRate\": ").append(timer.getOneMinuteRate()).append(COMMA_LF);
    sb.append("    \"fiveMinRate\": ").append(timer.getFiveMinuteRate()).append(COMMA_LF);
    sb.append("    \"fifteenMinRate\": ").append(timer.getFifteenMinuteRate()).append(LF);
    // TODO remaining fields
    /*
       "responseTime": {
       "count": 29382,
       "meanRate":12.185627192860734,
       "oneMinRate": 12.563,
       "fiveMinRate": 12.364,
       "fifteenMinRate": 12.126,
       "min":169916,
       "max":5608694,
       "mean":415041.00024926325,
       "stddev":652907.9633011606,
       "p50":293324.0,
       "p75":344914.0,
       "p95":543647.0,
       "p98":2706543.0,
       "p99":5608694.0,
       "p999":5608694.0
     }
*/
  }


  private Number getValueFromMetric(Metric value) {
    if (value instanceof Gauge) {
      Number value1 = (Number) ((Gauge) value).getValue();
      double v;
      if (value1 != null) {
        v = value1.doubleValue();
      } else {
        return -142.142; // TODO
      }
      return v;
    } else if (value instanceof Counter) {
      return ((Counter) value).getCount();
    } else {
      System.err.println("Not yet supported : " + value.getClass().getName());
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
        sb.append(LF);
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
    sb.append(LF);

    return sb;
  }

  @Override
  public String getContentType() {
    return "application/json";
  }
}
