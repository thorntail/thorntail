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
package org.wildfly.swarm.tools;

import java.io.File;

import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.wildfly.swarm.fractions.FractionDescriptor;

/**
 * @author Bob McWhirter
 */
public class ArtifactSpec extends MavenArtifactDescriptor {

    public final String scope;

    public File file;

    public boolean shouldGather = true;

    public ArtifactSpec(final String scope,
                        final String groupId,
                        final String artifactId,
                        final String version,
                        final String packaging,
                        final String classifier,
                        final File file) {
        super(groupId, artifactId, packaging, classifier, version);
        this.scope = scope;
        this.file = file;
    }

    public static ArtifactSpec fromMscGav(String gav) {
        String[] parts = gav.split(":");
        if (parts.length == 3) {
            return new ArtifactSpec("compile", parts[0], parts[1], parts[2], "jar", null, null);
        } else if (parts.length == 4) {
            return new ArtifactSpec("compile", parts[0], parts[1], parts[2], "jar", parts[3], null);
        } else {
            throw new RuntimeException("Invalid gav: " + gav);
        }
    }

    public FractionDescriptor toFractionDescriptor() {
        return new FractionDescriptor(groupId(), artifactId(), version());
    }

    public static ArtifactSpec fromFractionDescriptor(FractionDescriptor descriptor) {
        return fromMscGav(descriptor.toString());
    }

    public String jarName() {
        String classifier = classifier();
        if (classifier != null &&
                classifier.length() > 0) {
            classifier = "-" + classifier;
        } else {
            classifier = "";
        }

        return String.format("%s-%s%s.%s", artifactId(), version(), classifier, type());
    }

    public String jarRepoPath() {
        return String.format("%s/%s/%s/%s", groupId().replace('.', '/'), artifactId(),
                             version(), jarName());
    }

    public boolean isResolved() {
        return this.file != null;
    }

    public String toString() {
        return mavenGav() + " [" + this.scope + "]";
    }


}
