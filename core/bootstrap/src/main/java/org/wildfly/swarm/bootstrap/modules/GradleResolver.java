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

import org.jboss.modules.Module;
import org.jboss.modules.maven.ArtifactCoordinates;
import org.jboss.modules.maven.MavenArtifactUtil;
import org.jboss.modules.maven.MavenResolver;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This resolver try to find the requested artifact in the local gradle cache. If the artifact is missing, it try
 * to download it from a remote repository. Default is the https://repo1.maven.org/maven2/ repository. With the
 * system property 'remote.maven.repo' it's possible to specify additional repositories.
 *
 * @author Michael Fraefel
 */
public class GradleResolver implements MavenResolver {
    private final String gradleCachePath;
    private final List<String> remoteRepositories = new LinkedList();

    public GradleResolver(String gradleCachePath) {
        this.gradleCachePath = gradleCachePath;

        String remoteRepository = System.getProperty("remote.maven.repo");
        if (remoteRepository != null) {
            for (String repo : remoteRepository.split(",")) {
                if (!repo.endsWith("/")) {
                    this.remoteRepositories.add(repo + "/");
                } else {
                    this.remoteRepositories.add(repo);
                }
            }
        }
        this.remoteRepositories.add("https://repo1.maven.org/maven2/");
    }

    @Override
    public File resolveArtifact(ArtifactCoordinates artifactCoordinates, String packaging) throws IOException {
        //Search the matching artifact in a gradle cache.
        String filter = toGradleArtifactFileName(artifactCoordinates, packaging);
        Path artifactDirectory = Paths.get(gradleCachePath, artifactCoordinates.getGroupId(), artifactCoordinates.getArtifactId(), artifactCoordinates.getVersion());
        if (Files.exists(artifactDirectory)) {
            File latestArtifactFile = null;
            for (Path hashDir : Files.list(artifactDirectory).collect(Collectors.toList())) {
                for (Path artifact : Files.list(hashDir).collect(Collectors.toList())) {
                    if (artifact.endsWith(filter)) {
                        File artifactFile = artifact.toFile();
                        if (latestArtifactFile == null || latestArtifactFile.lastModified() < artifactFile.lastModified()) {
                            //take always the latest version of the artifact
                            latestArtifactFile = artifactFile;
                        }
                    }
                }
            }
            if (latestArtifactFile != null) {
                return latestArtifactFile;
            }
        }

        //Artifact not found in the locale gradle cache. Try to resolve it from the remote respository
        return downloadFromRemoteRepository(artifactCoordinates, packaging, artifactDirectory);
    }

    /**
     * Download artifact from remote repository.
     *
     * @param artifactCoordinates
     * @param packaging
     * @param artifactDirectory
     * @return
     */
    File downloadFromRemoteRepository(ArtifactCoordinates artifactCoordinates, String packaging, Path artifactDirectory) {
        String artifactRelativeHttpPath = artifactCoordinates.relativeArtifactPath('/');
        String artifactRelativeMetadataHttpPath = artifactCoordinates.relativeMetadataPath('/');
        for (String remoteRepos : remoteRepositories) {
            String artifactAbsoluteHttpPath = remoteRepos + artifactRelativeHttpPath + ".";
            File targetArtifactPomDirectory = artifactDirectory.resolve(computeGradleUUID(artifactCoordinates + ":pom")).toFile();
            File targetArtifactDirectory = artifactDirectory.resolve(computeGradleUUID(artifactCoordinates + ":" + packaging)).toFile();

            File artifactFile = doDownload(remoteRepos, artifactAbsoluteHttpPath, artifactRelativeHttpPath, artifactCoordinates, packaging, targetArtifactPomDirectory, targetArtifactDirectory);
            if (artifactFile != null) {
                return artifactFile; // Success
            }

            //Try to doDownload the snapshot version
            if (artifactCoordinates.isSnapshot()) {
                String remoteMetadataPath = remoteRepos + artifactRelativeMetadataHttpPath;
                try {
                    String timestamp = MavenArtifactUtil.downloadTimestampVersion(artifactCoordinates + ":" + packaging, remoteMetadataPath);

                    String timestampedArtifactRelativePath = artifactCoordinates.relativeArtifactPath('/', timestamp);
                    String artifactTimestampedAbsoluteHttpPath = remoteRepos + timestampedArtifactRelativePath + ".";
                    File targetTimestampedArtifactPomDirectory = artifactDirectory.resolve(computeGradleUUID(artifactCoordinates + ":" + timestamp + ":pom")).toFile();
                    File targetTimestampedArtifactDirectory = artifactDirectory.resolve(computeGradleUUID(artifactCoordinates + ":" + packaging)).toFile();

                    File snapshotArtifactFile = doDownload(remoteRepos, artifactTimestampedAbsoluteHttpPath, timestampedArtifactRelativePath, artifactCoordinates, packaging, targetTimestampedArtifactPomDirectory, targetTimestampedArtifactDirectory);
                    if (snapshotArtifactFile != null) {
                        return snapshotArtifactFile; //Success
                    }
                } catch (XPathExpressionException | IOException ex) {
                    Module.getModuleLogger().trace(ex, "Could not doDownload '%s' from '%s' repository", artifactRelativeHttpPath, remoteRepos);
                    // try next one
                }
            }
        }
        return null;
    }

    /**
     * Download the POM and the artifact file and return it.
     *
     * @param remoteRepos
     * @param artifactAbsoluteHttpPath
     * @param artifactRelativeHttpPath
     * @param artifactCoordinates
     * @param packaging
     * @param targetArtifactPomDirectory
     * @param targetArtifactDirectory
     * @return
     */
    File doDownload(String remoteRepos, String artifactAbsoluteHttpPath, String artifactRelativeHttpPath, ArtifactCoordinates artifactCoordinates, String packaging, File targetArtifactPomDirectory, File targetArtifactDirectory) {
        //Download POM
        File targetArtifactPomFile = new File(targetArtifactPomDirectory, toGradleArtifactFileName(artifactCoordinates, "pom"));
        try {
            MavenArtifactUtil.downloadFile(artifactCoordinates + ":pom", artifactAbsoluteHttpPath + "pom", targetArtifactPomFile);
        } catch (IOException e) {
            Module.getModuleLogger().trace(e, "Could not doDownload '%s' from '%s' repository", artifactRelativeHttpPath, remoteRepos);
            // try next one
        }

        //Download Artifact
        File targetArtifactFile = new File(targetArtifactDirectory, toGradleArtifactFileName(artifactCoordinates, packaging));
        try {
            MavenArtifactUtil.downloadFile(artifactCoordinates + ":" + packaging, artifactAbsoluteHttpPath + packaging, targetArtifactFile);
            if (targetArtifactFile.exists()) {
                return targetArtifactFile;
            }
        } catch (IOException e) {
            Module.getModuleLogger().trace(e, "Could not doDownload '%s' from '%s' repository", artifactRelativeHttpPath, remoteRepos);
            // try next one
        }
        return null;
    }

    /**
     * Build file name for artifact.
     *
     * @param artifactCoordinates
     * @param packaging
     * @return
     */
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


    /**
     * Compute gradle uuid for artifacts.
     *
     * @param content
     * @return
     */
    String computeGradleUUID(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            md.reset();
            byte[] bytes = content.trim().toLowerCase(Locale.US).getBytes("UTF-8");
            md.update(bytes, 0, bytes.length);
            return byteArrayToHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            // Impossible
            throw new IllegalArgumentException("unknown algorithm " + MD5_ALGORITHM, e);
        } catch (UnsupportedEncodingException e) {
            // Impossible except with IBM :)
            throw new IllegalArgumentException("unknown charset UTF-8", e);
        }
    }

    String byteArrayToHexString(byte[] in) {
        byte ch = 0x00;

        if (in == null || in.length <= 0) {
            return null;
        }

        StringBuffer out = new StringBuffer(in.length * 2);

        //CheckStyle:MagicNumber OFF
        for (byte b : in) {
            ch = (byte) (b & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4); // shift the bits down
            ch = (byte) (ch & 0x0F); // must do this is high order bit is on!

            out.append(CHARS[(int) ch]); // convert the nibble to a String Character
            ch = (byte) (b & 0x0F); // Strip off low nibble
            out.append(CHARS[(int) ch]); // convert the nibble to a String Character
        }
        //CheckStyle:MagicNumber ON

        return out.toString();
    }

    // algorithm for gradle uuid
    private static final String MD5_ALGORITHM = "md5";
    // byte to hex string converter
    private static final char[] CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

}
