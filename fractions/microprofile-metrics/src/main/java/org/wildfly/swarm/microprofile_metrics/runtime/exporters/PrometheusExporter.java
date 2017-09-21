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
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.wildfly.swarm.microprofile_metrics.runtime.MetricRegistryFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Export data in Prometheus text format
 * @author Heiko W. Rupp
 */
public class PrometheusExporter implements Exporter {


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

      switch (md.getTypeRaw()) {
          case GAUGE:
          case COUNTER:
              createSimpleValueLine(sb,scope,key,md,entry);
              break;
          default:
              System.err.println("Not yet supported: " + key);
              continue;

      }
    }
  }

    private void writeTypeLine(MetricRegistry.Type scope, StringBuilder sb, String key, Metadata md) {
        sb.append("# TYPE ");
        sb.append(scope.getName().toLowerCase());
        sb.append(':').append(key).append(" ").append(md.getType()).append("\n");
    }

    private void createSimpleValueLine(StringBuilder sb, MetricRegistry.Type scope, String key, Metadata md, Map.Entry<String, Metric> entry) {

        // value line
        sb.append(scope.getName().toLowerCase()).append(":").append(key);
        String tags = md.getTagsAsString();
        if (tags != null && !tags.isEmpty()) {
          sb.append('{').append(tags).append('}');
        }

        Double valIn;
        if (md.getTypeRaw().equals(MetricType.GAUGE)) {
            Number value1 = (Number) ((Gauge) entry.getValue()).getValue();
            if (value1 != null) {
                valIn = value1.doubleValue();
            } else {
                valIn = -42.242; // TODO
            }
        } else {
            valIn = (double) ((Counter) entry.getValue()).getCount();
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
        createSimpleValueLine(sb,scope,key,metadata,outMap.entrySet().iterator().next());
        return sb;
    }

    private String getPrometheusMetricName(Metadata entry, String name) {
      String out = name.replace('-', '_').replace('.', '_').replace(' ','_');
      out = decamelize(out);
      if (entry == null) {
        throw new IllegalStateException("No entry for " + name + "found");
      }
      if (entry.getUnit() == null) {
        throw new IllegalStateException("Entry " + entry + "has no unit ");
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
