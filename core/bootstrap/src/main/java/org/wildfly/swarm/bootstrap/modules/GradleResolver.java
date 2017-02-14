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
package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.jboss.modules.maven.MavenResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * @author Michael Fraefel
 */
public class GradleResolver implements MavenResolver {

    private final String gradleCachePath;

    public GradleResolver(String gradleCachePath) {
        this.gradleCachePath = gradleCachePath;
    }

    @Override
    public File resolveArtifact(ArtifactCoordinates artifactCoordinates, String packaging) throws IOException {
        //Search the matching artifact in a gradle cache.
        String filter = toGradleArtifactFileName(artifactCoordinates, packaging);
        Path artifactDirectory = Paths.get(gradleCachePath, artifactCoordinates.getGroupId(), artifactCoordinates.getArtifactId(), artifactCoordinates.getVersion());
        for (Path hashDir :Files.list(artifactDirectory).collect(Collectors.toList())) {
            for (Path artifact : Files.list(hashDir).collect(Collectors.toList())) {
                if (artifact.endsWith(filter)) {
                    return artifact.toFile();
                }
            }
        }
        return null;
    }

    String toGradleArtifactFileName(ArtifactCoordinates artifactCoordinates, String packaging) {
        StringBuilder sbFileFilter = new StringBuilder();
        sbFileFilter
                .append(artifactCoordinates.getArtifactId())
                .append("-")
                .append(artifactCoordinates.getVersion());
        if (artifactCoordinates.getClassifier() != null && artifactCoordinates.getClassifier().length() > 0) {
            sbFileFilter
                    .append("-")
                    .append(artifactCoordinates.getClassifier());
        }
        sbFileFilter
                .append(".")
                .append(packaging);
        return sbFileFilter.toString();
    }

}
