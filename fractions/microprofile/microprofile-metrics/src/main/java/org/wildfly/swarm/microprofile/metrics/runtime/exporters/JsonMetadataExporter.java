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

import java.util.Optional;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.wildfly.swarm.microprofile.metrics.runtime.MetricRegistries;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author hrupp
 */
public class JsonMetadataExporter implements Exporter {

    private static final String QUOTE_COMMA_LF = "\",\n";
    private static final String LF = "\n";

    @Override
    public StringBuilder exportOneScope(MetricRegistry.Type scope) {

        StringBuilder sb = new StringBuilder();

        getDataForOneScope(scope, sb);

        return sb;
    }

    private void getDataForOneScope(MetricRegistry.Type scope, StringBuilder sb) {
        MetricRegistry registry = MetricRegistries.get(scope);
        Map<String, Metadata> theMetadata = registry.getMetadata();

        sb.append("{");
        writeMetadataForMap(sb, theMetadata);
        sb.append("}");
    }

    private void writeMetadataForMap(StringBuilder sb, Map<String, Metadata> theMetadata) {
        Iterator<Map.Entry<String, Metadata>> iter = theMetadata.entrySet().iterator();
        while (iter.hasNext()) {
            Metadata entry = iter.next().getValue();
            sb.append('"').append(entry.getName()).append('"').append(": {\n");
            Optional<String> optUnit = entry.getUnit();
            String unit = optUnit.orElse("none");
            sb.append("  \"unit\": \"").append(unit).append(QUOTE_COMMA_LF);
            sb.append("  \"type\": \"").append(entry.getType()).append(QUOTE_COMMA_LF);
            if (entry.getDescription() != null) {
                sb.append("  \"description\": \"").append(entry.getDescription()).append(QUOTE_COMMA_LF);
            }
            if (!entry.getTags().isEmpty()) {
                sb.append("  \"tags\": \"");
                sb.append(getTagsAsString(entry.getTags()));
                sb.append(QUOTE_COMMA_LF);
            }
            sb.append("  \"displayName\": \"").append(entry.getDisplayName()).append("\"\n");
            if (iter.hasNext()) {
                sb.append("  },\n");
            } else {
                sb.append("  }\n");
            }
        }
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


    @Override
    public StringBuilder exportAllScopes() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        MetricRegistry.Type[] values = MetricRegistry.Type.values();
        int totalNonEmptyScopes = Helper.countNonEmptyScopes();

        int scopes = 0;
        for (int i = 0; i < values.length; i++) {
            MetricRegistry.Type scope = values[i];
            MetricRegistry registry = MetricRegistries.get(scope);

            if (registry.getNames().size() > 0) {
                sb.append('"').append(scope.getName().toLowerCase()).append('"').append(" :\n");
                getDataForOneScope(scope, sb);
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
        MetricRegistry registry = MetricRegistries.get(scope);
        Map<String, Metadata> metadataMap = registry.getMetadata();

        Metadata m = metadataMap.get(metricName);

        Map<String, Metadata> outMap = new HashMap<>(1);
        outMap.put(metricName, m);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        writeMetadataForMap(sb, outMap);
        sb.append("}");
        sb.append(LF);

        return sb;
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}
