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
package org.wildfly.swarm.bootstrap.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmDependenciesConf {

    public final static String CLASSPATH_LOCATION = "META-INF/wildfly-swarm-dependencies.conf";

    public WildFlySwarmDependenciesConf() {

    }

    public WildFlySwarmDependenciesConf(InputStream in) throws IOException {
        read(in);
    }

    public void read(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String line = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equals("")) {
                    continue;
                }

                if (line.startsWith("primary:")) {
                    this.primaryDependencies.add(MavenArtifactDescriptor.fromMscGav(line.substring(8)));
                } else if (line.startsWith("extra:")) {
                    this.extraDependencies.add(MavenArtifactDescriptor.fromMavenGav(line.substring(6)));
                }
            }
        }
    }

    public void write(OutputStream out) throws IOException {
        PrintWriter writer = new PrintWriter(out);

        for (MavenArtifactDescriptor dependency : this.primaryDependencies) {
            writer.println("primary:" + dependency.mscGav());
        }

        for (MavenArtifactDescriptor dependency : this.extraDependencies) {
            writer.println("extra:" + dependency.mavenGav());
        }

        writer.flush();
    }

    public String toString() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(out);
            out.close();
            return new String(out.toByteArray());
        } catch (IOException e) {
            return "";
        }
    }

    public void addPrimaryDependency(MavenArtifactDescriptor dep) {
        this.primaryDependencies.add(dep);
    }

    public List<MavenArtifactDescriptor> getPrimaryDependencies() {
        return Collections.unmodifiableList(this.primaryDependencies);
    }

    public void addExtraDependency(MavenArtifactDescriptor dep) {
        this.extraDependencies.add(dep);
    }

    public List<MavenArtifactDescriptor> getExtraDependencies() {
        return Collections.unmodifiableList(this.extraDependencies);
    }

    public MavenArtifactDescriptor find(String groupId, String artifactId, String packaging, String classifier) {
        if (classifier != null && classifier.trim().isEmpty()) {
            classifier = null;
        }

        for (MavenArtifactDescriptor each : this.primaryDependencies) {
            if (!each.groupId().equals(groupId)) {
                continue;
            }
            if (!each.artifactId().equals(artifactId)) {
                continue;
            }
            if (!each.type().equals(packaging)) {
                continue;
            }
            if (classifier == null && each.classifier() == null) {
                return each;
            }
            if (classifier == null || each.classifier() == null) {
                continue;
            }
            if (!each.classifier().equals(classifier)) {
                continue;
            }

            return each;
        }

        for (MavenArtifactDescriptor each : this.extraDependencies) {
            if (!each.groupId().equals(groupId)) {
                continue;
            }
            if (!each.artifactId().equals(artifactId)) {
                continue;
            }
            if (!each.type().equals(packaging)) {
                continue;
            }

            if (classifier == null && each.classifier() == null) {
                return each;
            }
            if (classifier == null || each.classifier() == null) {
                continue;
            }
            if (!each.classifier().equals(classifier)) {
                continue;
            }

            return each;
        }

        return null;
    }

    private List<MavenArtifactDescriptor> primaryDependencies = new ArrayList<>();

    private List<MavenArtifactDescriptor> extraDependencies = new ArrayList<>();
}
