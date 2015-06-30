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

    public String toString() {
        return this.groupId + ":" + this.artifactId + ":" + this.version + ":" + this.packaging + (this.classifier == null ? "" : this.classifier) + ":" + this.file + " [" + this.scope + "]";
    }
}
