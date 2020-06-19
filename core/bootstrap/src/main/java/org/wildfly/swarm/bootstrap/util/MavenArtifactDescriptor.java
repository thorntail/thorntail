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
package org.wildfly.swarm.bootstrap.util;

import java.io.File;
import java.io.IOException;

import org.jboss.modules.maven.ArtifactCoordinates;

/**
 * @author Bob McWhirter
 */
public class MavenArtifactDescriptor implements Comparable<MavenArtifactDescriptor> {

    private static final String INVALID_GAV_MESSAGE = "Invalid gav: ";

    private static final String JAR_PACKAGING = "jar";

    private static final String COLON = ":";

    private MavenArtifactDescriptor() {
    }

    public MavenArtifactDescriptor(String groupId, String artifactId, String version) {
        this(groupId, artifactId, JAR_PACKAGING, null, version);
    }

    public MavenArtifactDescriptor(String groupId, String artifactId, String type, String classifier, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        if (classifier != null && !classifier.trim().equals("")) {
            this.classifier = classifier;
        }
    }

    public static Builder build() {
        return new MavenArtifactDescriptor().builder().type(JAR_PACKAGING);
    }

    /**
     * from JBoss Modules style (not MSC, method name is wrong!): {@code groupId:artifactId:version[:classifier]}
     */
    public static MavenArtifactDescriptor fromMscGav(String gav) throws IOException {
        String[] parts = gav.split(COLON);
        if (parts.length == 3) {
            return new MavenArtifactDescriptor(parts[0], parts[1], parts[2]);
        } else if (parts.length == 4) {
            return new MavenArtifactDescriptor(parts[0], parts[1], JAR_PACKAGING, parts[3], parts[2]);
        } else {
            throw new IOException(INVALID_GAV_MESSAGE + gav);
        }
    }

    /**
     * from Maven (Aether) style: {@code groupId:artifactId[:packaging[:classifier]]:version}
     */
    public static MavenArtifactDescriptor fromMavenGav(String gav) throws IOException {
        String[] parts = gav.split(COLON);

        if (parts.length == 3) {
            return new MavenArtifactDescriptor(parts[0], parts[1], parts[2]);
        } else if (parts.length == 4) {
            return new MavenArtifactDescriptor(parts[0], parts[1], parts[2], null, parts[3]);
        } else if (parts.length == 5) {
            return new MavenArtifactDescriptor(parts[0], parts[1], parts[2], parts[3], parts[4]);
        } else if (parts.length == 6) {
            return new MavenArtifactDescriptor(parts[0], parts[1], parts[2], parts[3], parts[4]);
        } else {
            throw new IOException(INVALID_GAV_MESSAGE + gav);
        }

    }

    @Override
    public int compareTo(MavenArtifactDescriptor that) {
        int result = this.groupId.compareTo(that.groupId);
        if (result != 0) {
            return result;
        }

        result = this.artifactId.compareTo(that.artifactId);
        if (result != 0) {
            return result;
        }

        result = this.version.compareTo(that.version);
        if (result != 0) {
            return result;
        }

        if (this.type != null && that.type == null) {
            return 1;
        }

        if (this.type == null && that.type != null) {
            return -1;
        }

        result = this.type.compareTo(that.type);

        if (result != 0) {
            return result;
        }

        if (this.classifier != null && that.classifier == null) {
            return 1;
        }

        if (this.classifier == null && that.classifier != null) {
            return -1;
        }

        return this.classifier.compareTo(that.classifier);
    }

    public String groupId() {
        return this.groupId;
    }

    public String artifactId() {
        return this.artifactId;
    }

    public String version() {
        return this.version;
    }

    public String classifier() {
        return this.classifier;
    }

    public String type() {
        return this.type;
    }

    public String mscGav() {
        return this.groupId + COLON +
                this.artifactId + COLON +
                this.version +
                (this.classifier == null ? "" : COLON + this.classifier);
    }

    public ArtifactCoordinates mscCoordinates() {
        return new ArtifactCoordinates(this.groupId,
                                       this.artifactId,
                                       this.version,
                                       this.classifier == null ? "" : this.classifier);

    }

    public String mavenGav() {
        return this.groupId + COLON +
                this.artifactId + COLON +
                (this.type == null ? JAR_PACKAGING : this.type) + COLON +
                (this.classifier == null ? "" : this.classifier + COLON) +
                this.version;
    }

    public String repoPath(boolean forJar) {
        char delim = File.separatorChar;

        if (forJar) {
            delim = '/';
        }

        String[] groupParts = this.groupId.split("\\.");

        StringBuffer p = new StringBuffer();

        for (String groupPart : groupParts) {
            p.append(groupPart)
                    .append(delim);
        }

        p.append(this.artifactId)
                .append(delim);

        p.append(this.version)
                .append(delim);

        p.append(this.artifactId)
                .append('-')
                .append(this.version);

        if (this.classifier != null) {
            p.append('-')
                    .append(this.classifier);
        }

        p.append('.').append(this.type);

        return p.toString();
    }

    public String toString() {
        return mscGav();
    }

    private Builder builder() {
        return new Builder();
    }

    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MavenArtifactDescriptor that = (MavenArtifactDescriptor) o;

        if (!groupId.equals(that.groupId)) {
            return false;
        }
        if (!artifactId.equals(that.artifactId)) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }
        if (classifier != null ? !classifier.equals(that.classifier) : that.classifier != null) {
            return false;
        }
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }

    public class Builder {

        public Builder groupId(String groupId) {
            MavenArtifactDescriptor.this.groupId = groupId;
            return this;
        }

        public Builder artifactId(String artifactId) {
            MavenArtifactDescriptor.this.artifactId = artifactId;
            return this;
        }

        public Builder version(String version) {
            MavenArtifactDescriptor.this.version = version;
            return this;
        }

        public Builder type(String type) {
            MavenArtifactDescriptor.this.type = type;
            return this;
        }

        public Builder classifier(String classifier) {
            MavenArtifactDescriptor.this.classifier = classifier;
            return this;
        }

        public MavenArtifactDescriptor build() {
            return MavenArtifactDescriptor.this;
        }
    }
}
