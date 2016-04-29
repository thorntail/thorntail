/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.fractionlist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.wildfly.swarm.tools.FractionDescriptor;
import org.wildfly.swarm.tools.PropertiesUtil;

/**
 * @author Bob McWhirter
 */
public class FractionList implements org.wildfly.swarm.tools.FractionList {

    private final Map<String, FractionDescriptor> descriptors = new TreeMap<>();

    private static final AtomicReference<FractionList> INSTANCE = new AtomicReference<>();

    public static FractionList get() {
        return INSTANCE.updateAndGet(old -> old != null ? old : new FractionList());
    }

    private FractionList() {
        try (InputStreamReader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("fraction-list.json"))) {
            Json.parse(reader).asArray().forEach(entry -> {
                JsonObject fraction = entry.asObject();
                String groupId = fraction.getString("groupId", null);
                String artifactId = fraction.getString("artifactId", null);
                String version = fraction.getString("version", null);
                String name = fraction.getString("name", null);
                String description = fraction.getString("description", null);
                String tags = fraction.getString("tags", null);
                boolean internal = fraction.getBoolean("internal", false);
                FractionDescriptor fd = new FractionDescriptor(groupId, artifactId, version, name, description, tags, internal);
                descriptors.put(groupId + ":" + artifactId, fd);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Set up dependencies
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("fraction-list.txt")))) {

            String line = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] sides = line.split("=");

                String lhs = sides[0].trim();
                String lhsKey = toKey(lhs);
                FractionDescriptor desc = this.descriptors.get(lhsKey);
                if (desc == null) {
                    String[] gavParts = lhs.split(":");
                    desc = new FractionDescriptor(gavParts[0], gavParts[1], gavParts[2]);
                    this.descriptors.put(lhsKey, desc);
                }

                if (sides.length > 1) {
                    String rhs = sides[1].trim();
                    String[] deps = rhs.split(",");

                    for (String dep : deps) {
                        dep = dep.trim();
                        if (dep.isEmpty()) {
                            continue;
                        }
                        String depKey = toKey(dep);
                        FractionDescriptor depDesc = this.descriptors.get(depKey);
                        if (depDesc == null) {
                            String[] gavParts = dep.split(":");
                            depDesc = new FractionDescriptor(gavParts[0], gavParts[1], gavParts[2]);
                            this.descriptors.put(depKey, depDesc);
                        }
                        desc.addDependency(depDesc);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param a groupId:artifactId:version string
     * @return the groupId:artifactId
     */
    private String toKey(String gav) {
        return gav.substring(0, gav.lastIndexOf(':'));
    }

    @Override
    public Collection<FractionDescriptor> getFractionDescriptors() {
        return Collections.unmodifiableCollection(this.descriptors.values());
    }

    @Override
    public FractionDescriptor getFractionDescriptor(final String groupId, final String artifactId) {
        return this.descriptors.get(groupId + ":" + artifactId);
    }

    @Override
    public Map<String, FractionDescriptor> getPackageSpecs() {
        final Map<String, String> packageSpecs = loadPackageSpecs();

        return this.descriptors.values().stream()
                .filter(fd -> packageSpecs.containsKey(fd.artifactId()))
                .collect(Collectors.toMap(fd -> packageSpecs.get(fd.artifactId()),
                                          fd -> fd));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Map<String, String> loadPackageSpecs() {
        try {
            final InputStream in = FractionList.class.getClassLoader()
                    .getResourceAsStream("org/wildfly/swarm/fractionlist/fraction-packages.properties");

            return new HashMap<>((Map) PropertiesUtil.loadProperties(in));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load fraction-packages.properties", e);
        }

    }
}
