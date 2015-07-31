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

    public boolean shouldGather = false;

    public ArtifactSpec(String scope, String groupId, String artifactId, String version, String packaging, String classifier, File file) {
        this.scope = scope;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
        this.classifier = classifier;
        this.file = file;
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
}
