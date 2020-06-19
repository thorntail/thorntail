/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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

    private static final String INVALID_GAV_MESSAGE = "Invalid gav: ";

    private static final String JAR_PACKAGING = "jar";

    private static final String COMPILE_SCOPE = "compile";

    public final String scope;

    public String sha1sum;

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
        this.sha1sum = null;
    }

    private ArtifactSpec(final String groupId,
                        final String artifactId,
                        final String version,
                        final String classifier,
                        final String sha1sum) {
        super(groupId, artifactId, JAR_PACKAGING, classifier, version);
        this.sha1sum = sha1sum;
        this.scope = COMPILE_SCOPE;
    }

    /**
     * from JBoss Modules style (not MSC, method name is wrong!): {@code groupId:artifactId:version[:classifier]}
     */
    public static ArtifactSpec fromMscGav(String gav) {
        String[] parts = gav.split(":");
        if (parts.length == 3) {
            return new ArtifactSpec(COMPILE_SCOPE, parts[0], parts[1], parts[2], JAR_PACKAGING, null, null);
        } else if (parts.length == 4) {
            return new ArtifactSpec(COMPILE_SCOPE, parts[0], parts[1], parts[2], JAR_PACKAGING, parts[3], null);
        } else {
            throw new RuntimeException(INVALID_GAV_MESSAGE + gav);
        }
    }

    /**
     * from Maven (Aether) style: {@code groupId:artifactId[:packaging[:classifier]]:version}
     */
    public static ArtifactSpec fromMavenGav(String gav) {
        String[] parts = gav.split(":");
        if (parts.length == 3) {
            return new ArtifactSpec(COMPILE_SCOPE, parts[0], parts[1], parts[2], JAR_PACKAGING, null, null);
        } else if (parts.length == 4) {
            return new ArtifactSpec(COMPILE_SCOPE, parts[0], parts[1], parts[3], parts[2], null, null);
        } else if (parts.length == 5) {
            return new ArtifactSpec(COMPILE_SCOPE, parts[0], parts[1], parts[4], parts[2], parts[3], null);
        } else if (parts.length == 6) {
            return new ArtifactSpec(COMPILE_SCOPE, parts[0], parts[1], parts[4], parts[2], parts[3], null);
        } else {
            throw new RuntimeException(INVALID_GAV_MESSAGE + gav);
        }
    }

    public static ArtifactSpec fromMavenDependencyDescription(String description) {
        String[] parts = description.split("#");
        ArtifactSpec result = fromMscGav(parts[0]);
        result.sha1sum = parts[1];
        return result;
    }

    public FractionDescriptor toFractionDescriptor() {
        return new FractionDescriptor(groupId(), artifactId(), version());
    }

    public static ArtifactSpec fromFractionDescriptor(FractionDescriptor descriptor) {
        return fromMscGav(descriptor.toString());
    }

    public String jarName() {
        String classifier = classifier();
        if (classifier != null && classifier.length() > 0) {
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
