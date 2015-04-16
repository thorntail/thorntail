package org.wildfly.swarm.plugin;

import org.apache.maven.artifact.Artifact;

/**
 * @author Bob McWhirter
 */
public class ArtifactSpec {

    private static final String JANDEX_SUFFIX = "?jandex";

    private String groupId;
    private String artifactId;
    private String classifier;
    private boolean jandex;

    public ArtifactSpec(String gav) {
        String[] parts = gav.split( ":" );

        this.groupId = parts[0];
        this.artifactId = parts[1];

        if ( this.artifactId.endsWith( JANDEX_SUFFIX ) ) {
            this.artifactId = this.artifactId.substring( 0, this.artifactId.length() - JANDEX_SUFFIX.length() );
            this.jandex = true;
        }

        if ( parts.length >= 4) {
            this.classifier = parts[3];
        } else {
            this.classifier = null;
        }
    }

    public boolean matches(Artifact artifact) {
        if ( ! this.groupId.equals( artifact.getGroupId() ) ){
            return false;
        }

        if ( ! this.artifactId.equals( artifact.getArtifactId() ) ){
            return false;
        }

        if ( this.classifier == null ) {
            return artifact.getClassifier() == null || artifact.getClassifier().equals( "" );
        }

        return this.classifier.equals( artifact.getClassifier() );
    }

    public String toString() {
        return this.groupId + ":" + this.artifactId + ( this.classifier == null ? "" : "::" + this.classifier);
    }
}
