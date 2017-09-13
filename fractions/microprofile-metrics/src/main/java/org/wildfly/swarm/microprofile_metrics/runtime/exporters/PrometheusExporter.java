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

import java.util.Map;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.wildfly.swarm.microprofile_metrics.runtime.MetricRegistryFactory;

/**
 * Export data in Prometheus text format
 * @author Heiko W. Rupp
 */
public class PrometheusExporter implements Exporter {


  public StringBuilder exportOneScope(MetricRegistry.Type scope, Map<String,Double>values) {

    StringBuilder sb = new StringBuilder();
    MetricRegistry registry = MetricRegistryFactory.get(scope);

    for (Map.Entry<String,Double> entry: values.entrySet()) {
      String key = entry.getKey();
      Metadata md = registry.getMetadata().get(key);

      key = getPrometheusMetricName(md,key);
      sb.append("# TYPE ").append("base:").append(key).append(" ").append(md.getType()).append("\n");
      sb.append("base:").append(key).append(" ").append(entry.getValue()).append("\n");
    }

    return sb;
  }

  @Override
  public StringBuilder exportAllScopes(Map<MetricRegistry.Type, Map<String, Double>> scopeValuesMap) {
    return new StringBuilder();
  }

  @Override
  public String getContentType() {
    return "text/plain";
  }

  private String getPrometheusMetricName(Metadata entry, String name) {
      String out = name.replace('-', '_').replace('.', '_').replace(' ','_');
      out = decamelize(out);
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
