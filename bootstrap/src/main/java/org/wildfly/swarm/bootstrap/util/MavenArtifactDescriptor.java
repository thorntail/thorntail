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

import java.io.File;
import java.io.IOException;

import org.jboss.modules.maven.ArtifactCoordinates;

/**
 * @author Bob McWhirter
 */
public class MavenArtifactDescriptor {

    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String type;

    private MavenArtifactDescriptor() {

    }

    public MavenArtifactDescriptor(String groupId, String artifactId, String version) {
        this(groupId, artifactId,  "jar", null, version);
    }

    public MavenArtifactDescriptor(String groupId, String artifactId, String type, String classifier, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        if ( classifier != null && ! classifier.trim().equals( "" ) ) {
            this.classifier = classifier;
        }
    }

    public static Builder build() {
        return new MavenArtifactDescriptor().builder().type("jar");
    }

    public static MavenArtifactDescriptor fromMscGav(String gav) throws IOException {
        String[] parts = gav.split(":");
        if ( parts.length == 3 ) {
            return new MavenArtifactDescriptor( parts[0], parts[1], parts[2] );
        } else if (parts.length ==4) {
            return new MavenArtifactDescriptor( parts[0], parts[1], "jar", parts[3], parts[2] );
        } else {
            throw new IOException("Invalid gav: " + gav );
        }
    }

    public static MavenArtifactDescriptor fromMavenGav(String gav) throws IOException {
        String[] parts = gav.split(":");

        if ( parts.length == 3 ) {
            return new MavenArtifactDescriptor( parts[0], parts[1], parts[2] );
        } else if (parts.length ==4 ) {
            return new MavenArtifactDescriptor( parts[0], parts[1], parts[2], null, parts[3] );
        } else if ( parts.length == 5 ) {
            return new MavenArtifactDescriptor( parts[0], parts[1], parts[2], parts[3], parts[4] );
        } else {
            throw new IOException("Invalid gav: " + gav );
        }

    }

    private Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof MavenArtifactDescriptor)) {
            return false;
        }
        return this.mavenGav().equals(((MavenArtifactDescriptor) obj).mavenGav());
    }

    @Override
    public int hashCode() {
        return mavenGav().hashCode();
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
        return this.groupId + ":" +
                this.artifactId + ":" +
                this.version +
                (this.classifier == null ? "" : ":" + this.classifier);
    }

    public ArtifactCoordinates mscCoordinates() {
        return new ArtifactCoordinates( this.groupId,
                this.artifactId,
                this.version,
                this.classifier == null ? "" : this.classifier );

    }

    public String mavenGav() {
        return this.groupId + ":" +
                this.artifactId + ":" +
                (this.type == null ? "jar" : this.type) + ":" +
                (this.classifier == null ? "" : this.classifier + ":") +
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

        p.append( this.artifactId)
                .append('-')
                .append( this.version );

        if ( this.classifier != null ) {
            p.append( '-' )
                    .append( this.classifier );
        }

        p.append( '.' ).append( this.type );

        return p.toString();
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
