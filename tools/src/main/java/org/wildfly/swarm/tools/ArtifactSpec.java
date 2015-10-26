/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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

/**
 * @author Bob McWhirter
 */
public class ArtifactSpec {

    public final String scope;
    public final String groupId;
    public final String artifactId;
    public final String version;
    public final String packaging;
    public final String classifier;
    public File file;

    public boolean shouldGather = true;
    public boolean gathered = false;

    public ArtifactSpec(String scope, String groupId, String artifactId, String version, String packaging, String classifier, File file) {
        this.scope = scope;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
        this.classifier = classifier;
        this.file = file;
    }

    public String getFileName() {
        return this.artifactId + ( this.classifier == null || this.classifier.equals( "" ) ? "" : "-" + classifier ) + "-" + this.version + "." + this.packaging;
    }

    public int hashCode() {
        int parts = 5;
        return ((this.groupId.hashCode() / parts) +
                (this.artifactId.hashCode() / parts) +
                (this.version.hashCode() / parts) +
                (this.packaging.hashCode() / parts)) +
                ( (this.classifier != null ? ( this.packaging.hashCode() / parts) : 0 ));
    }

    public boolean equals(Object other) {
        if ( ! ( other instanceof ArtifactSpec ) ) {
            return false;
        }

        ArtifactSpec that = (ArtifactSpec) other;

        if ( ! this.groupId.equals( that.groupId ) ) {
            return false;
        }

        if ( ! this.artifactId.equals( that.artifactId ) ) {
            return false;
        }

        if ( ! this.version.equals( that.version ) ) {
            return false;
        }

        if ( ! this.packaging.equals( that.packaging ) ) {
            return false;
        }

        if ( this.classifier == null ) {
            if ( that.classifier != null ) {
                return false;
            }
        } else  if ( ! this.classifier.equals( that.classifier ) ) {
            return false;
        }

        return true;
    }

    public String toString() {
        return coordinates() + " [" + this.scope + "]";
    }

    public String coordinates() {
        return this.groupId + ":" + this.artifactId + ":" + this.packaging + ":" + (this.classifier == null ? "" : this.classifier + ":" ) + this.version;
    }

    public String mscCoordinates() {
        return this.groupId + ":" + this.artifactId + ":" + this.version + (this.classifier == null ? "" : ( ":" + this.classifier ) );
    }
}
