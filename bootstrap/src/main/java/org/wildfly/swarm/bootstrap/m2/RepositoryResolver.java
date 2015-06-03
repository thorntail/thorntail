package org.wildfly.swarm.bootstrap.m2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public abstract class RepositoryResolver {

    protected static final String SEPARATOR = "/";

    private static final String JANDEX_SUFFIX = "?jandex";

    public abstract File resolve(String gav) throws IOException;

    protected Path gavToPath(String gav) {
        String[] parts = gav.split(":");
        String group = parts[0];
        String artifact = parts[1];
        String version = parts[2];
        String classifier = null;
        if (parts.length >= 4) {
            classifier = parts[3];
        }

        if (artifact.endsWith(JANDEX_SUFFIX)) {
            artifact = artifact.substring(0, artifact.length() - JANDEX_SUFFIX.length());
        }

        StringBuilder path = new StringBuilder();


        path.append(group.replaceAll("\\.", SEPARATOR));
        path.append(SEPARATOR);
        path.append(artifact);
        path.append(SEPARATOR);
        path.append(version);
        path.append(SEPARATOR);
        path.append(artifact);
        path.append("-");
        path.append(version);

        if (classifier != null && !classifier.equals("")) {
            path.append("-").append(classifier);
        }

        path.append(".jar");
        return Paths.get(path.toString());
    }

}
