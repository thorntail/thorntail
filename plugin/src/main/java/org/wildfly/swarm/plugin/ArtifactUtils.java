package org.wildfly.swarm.plugin;

import org.apache.maven.artifact.Artifact;

/**
 * @author Bob McWhirter
 */
public class ArtifactUtils {

    public static String toPath(Artifact artifact) {

        StringBuilder path = new StringBuilder();
        path.append(artifact.getGroupId().replace('.', '/'));
        path.append("/");
        path.append(artifact.getArtifactId());
        path.append("/");
        path.append(artifact.getVersion());
        path.append("/");
        path.append(artifact.getArtifactId());
        path.append("-");
        path.append(artifact.getVersion());

        if (artifact.getClassifier() != null && !artifact.getClassifier().equals("")) {
            path.append("-");
            path.append(artifact.getClassifier());
        }

        path.append(".");
        path.append(artifact.getType());

        return path.toString();
    }
}
