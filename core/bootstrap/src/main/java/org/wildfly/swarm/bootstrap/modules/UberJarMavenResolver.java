/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

/**
 * @author Bob McWhirter
 */
public class UberJarMavenResolver implements ArtifactResolver {

    private static final String HYPHEN = "-";

    private static final String DOT = ".";

    private Map<ArtifactCoordinates, File> resolutionCache = new ConcurrentHashMap<>();

    public static File copyTempJar(String artifactId, InputStream in, String packaging) throws IOException {
        File tmp = TempFileManager.INSTANCE.newTempFile(artifactId, DOT + packaging);
        Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tmp;
    }

    @Override
    public ArtifactResolution resolveArtifact(ArtifactCoordinates coordinates, String packaging) throws IOException {


        String artifactRelativePath = "m2repo/" + relativeArtifactPath('/', coordinates.getGroupId(), coordinates.getArtifactId(), coordinates.getVersion());
        String classifier = "";
        if (coordinates.getClassifier() != null && !coordinates.getClassifier().trim().isEmpty()) {
            classifier = HYPHEN + coordinates.getClassifier();
        }

        String jarPath = artifactRelativePath + classifier + DOT + packaging;

        InputStream stream = UberJarMavenResolver.class.getClassLoader().getResourceAsStream(jarPath);

        if (stream != null) {
            //resolved = copyTempJar(coordinates.getArtifactId() + HYPHEN + coordinates.getVersion(), stream, packaging);
            //this.resolutionCache.put(coordinates, resolved);
            return new ArtifactResolution.StreamArtifactResolution(coordinates, packaging,coordinates.getArtifactId() + "-" + UUID.randomUUID().toString() + "." + packaging, stream);
        }

        return null;
    }

    static String relativeArtifactPath(char separator, String groupId, String artifactId, String version) {
        StringBuilder builder = new StringBuilder(groupId.replace('.', separator));
        builder.append(separator).append(artifactId).append(separator);
        String pathVersion;
        final Matcher versionMatcher = snapshotPattern.matcher(version);
        if (versionMatcher.find()) {
            // it's really a snapshot
            pathVersion = version.substring(0, versionMatcher.start()) + "-SNAPSHOT";
        } else {
            pathVersion = version;
        }
        builder.append(pathVersion).append(separator).append(artifactId).append(HYPHEN).append(version);
        return builder.toString();
    }

    private static final Pattern snapshotPattern = Pattern.compile("-\\d{8}\\.\\d{6}-\\d+$");
}
