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
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.wildfly.swarm.microprofile_metrics.runtime.MetricRegistryFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author hrupp
 */
public class JsonExporter implements Exporter {

  @Override
  public StringBuilder exportOneScope(MetricRegistry.Type scope) {

    StringBuilder sb = new StringBuilder();

    getMetricsForAScope(sb,scope);

    return sb;
  }

  private void getMetricsForAScope(StringBuilder sb, MetricRegistry.Type scope) {

    MetricRegistry registry = MetricRegistryFactory.get(scope);
    Map<String,Metric> metricMap = registry.getMetrics();

    sb.append("{\n");

    writeMetricsForMap(sb, metricMap);

    sb.append("}");
  }

  private void writeMetricsForMap(StringBuilder sb, Map<String, Metric> metricMap) {
    for (Iterator<Map.Entry<String, Metric>> iterator = metricMap.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry<String, Metric> entry = iterator.next();
      String key = entry.getKey();

      Metric value = entry.getValue();
      double val = getValueFromMetric(value);
      sb.append("  ").append('"').append(key).append('"').append(" : ").append(val);
      if (iterator.hasNext()) {
        sb.append(',');
      }
      sb.append("\n");
    }
  }

  private double getValueFromMetric(Metric value) {
    if (value instanceof Gauge) {
      Number value1 = (Number) ((Gauge) value).getValue();
      double v;
      if (value1 != null) {
        v = value1.doubleValue();
      }
      else {
        return -142.142; // TODO
      }
      return v;
    } else if (value instanceof Counter) {
      return ((Counter) value).getCount();
    } else {
      System.err.println("Not yet supported : " + value);
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

    Metric m = metricMap.get(metricName);

    Map<String,Metric> outMap = new HashMap<>(1);
    outMap.put(metricName,m);

    StringBuilder sb = new StringBuilder();
    sb.append("{");
    writeMetricsForMap(sb,outMap);
    sb.append("\n");

    return sb;
  }

  @Override
  public String getContentType() {
    return "application/json";
  }
}
