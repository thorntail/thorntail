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

import java.util.Iterator;
import java.util.Map;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.logging.Logger;

/**
 * @author hrupp
 */
public class JsonExporter implements Exporter {

  private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

  @Override
  public StringBuilder exportOneScope(MetricRegistry.Type scope, Map<String, Double> values) {

    StringBuilder sb = new StringBuilder();

    getMetricsForAScope(values, sb);

    return sb;
  }

  private void getMetricsForAScope(Map<String, Double> values, StringBuilder sb) {


    sb.append("{\n");

    for (Iterator<Map.Entry<String, Double>> iterator = values.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry<String, Double> entry = iterator.next();
      String key = entry.getKey();

      sb.append("  ").append('"').append(key).append('"').append(" : ").append(entry.getValue());
      if (iterator.hasNext()) {
        sb.append(',');
      }
      sb.append("\n");
    }

    sb.append("}");
  }

  @Override
  public StringBuilder exportAllScopes(Map<MetricRegistry.Type, Map<String, Double>> scopeValuesMap) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");

    MetricRegistry.Type[] values = MetricRegistry.Type.values();
    int totalNonEmptyScopes = 0;
    for (int i = 0; i < values.length; i++) {
      MetricRegistry.Type scope = values[i];
      if (scopeValuesMap.get(scope).size() > 0) {
        totalNonEmptyScopes++;
      }
    }

    int scopes = 0;
    for (int i = 0; i < values.length; i++) {
      MetricRegistry.Type scope = values[i];

      if (scopeValuesMap.get(scope).size() > 0) {
        sb.append('"').append(scope.getName().toLowerCase()).append('"').append(" :\n");
        getMetricsForAScope(scopeValuesMap.get(scope), sb);
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
  public String getContentType() {
    return "application/json";
  }
}
