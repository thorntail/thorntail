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
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.wildfly.swarm.microprofile_metrics.runtime.MetricRegistryFactory;

/**
 * @author hrupp
 */
public class JsonMetadataExporter extends AbstractExporter implements Exporter {

  private static final String QUOTE_COMMA_LF = "\",\n";

  @Override
  public StringBuilder exportOneScope(MetricRegistry.Type scope, Map<String, Double> values) {

    StringBuilder sb = new StringBuilder();

    getDataForOneScope(scope, sb);

    return sb;
  }

  private void getDataForOneScope(MetricRegistry.Type scope, StringBuilder sb) {
    MetricRegistry registry = MetricRegistryFactory.get(scope);
    Map<String,Metadata> theMetadata = registry.getMetadata();

    sb.append("{");
    Iterator<Map.Entry<String,Metadata>> iter = theMetadata.entrySet().iterator();
    while (iter.hasNext()) {
      Metadata entry = iter.next().getValue();
      sb.append('"').append(entry.getName()).append('"').append(": {\n");
      sb.append("  \"unit\": \"").append(entry.getUnit()).append(QUOTE_COMMA_LF);
      sb.append("  \"type\": \"").append(entry.getType()).append(QUOTE_COMMA_LF);
      sb.append("  \"description\": \"").append(entry.getDescription()).append(QUOTE_COMMA_LF);
      if (!entry.getTags().isEmpty()) {
        sb.append("  \"tags\": \"");
        sb.append(getTagsAsString(entry.getTags()));
        sb.append(QUOTE_COMMA_LF);
      }
      sb.append("  \"displayName\": \"").append(entry.getDisplayName()).append("\"\n");
      if (iter.hasNext()) {
        sb.append("},\n");
      } else {
        sb.append("}\n");
      }
    }
    sb.append("}");
  }

  private String getTagsAsString(Map<String, String> tags) {
    StringBuilder result = new StringBuilder();
    Iterator<Map.Entry<String, String>> iterator = tags.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> pair = iterator.next();
      result.append(pair.getKey()).append("=").append(pair.getValue());
      if (iterator.hasNext()) {
        result.append(",");
      }
     }
     return result.toString();
  }

  /*
  {
    "fooVal": {
      "unit": "milliseconds",
      "type": "gauge",
      "description": "The average duration of foo requests during last 5 minutes",
      "displayName": "Duration of foo",
      "tags": "app=webshop"
    },
    "barVal": {
      "unit": "megabytes",
      "type": "gauge",
      "tags": "component=backend,app=webshop"
    }
  }

   */

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
        getDataForOneScope(scope,sb);
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
