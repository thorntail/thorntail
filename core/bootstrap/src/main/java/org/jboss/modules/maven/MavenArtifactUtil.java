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
package org.jboss.modules.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jboss.modules.Module;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaders;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.util.JarFileManager;
import org.xml.sax.InputSource;

/**
 * Helper class to resolve a maven artifact.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:tcerar@redhat.com">Tomaz Cerar</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class MavenArtifactUtil {

    private static BootstrapLogger LOGGER = BootstrapLogger.logger("org.wildfly.swarm.bootstrap");

    static final Object artifactLock = new Object();

    private static final XPath xpath = XPathFactory.newInstance().newXPath();

    private static XPathExpression snapshotVersionXpath;

    private static final Pattern tempFilePattern = Pattern.compile("thorntail\\S+[0-9]{5,}.\\S{5,}");

    static {
        try {
            snapshotVersionXpath = xpath.compile("metadata/versioning/snapshotVersions/snapshotVersion[1]/value");
        } catch (XPathExpressionException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Try to resolve a Maven JAR artifact.  Calling this method is identical to calling
     * {@code resolveJarArtifact(qualifier, "jar")}.
     *
     * @param coordinates the non-{@code null} Maven coordinates object
     * @return the absolute path to the artifact, or {@code null} if none exists
     * @throws IOException if acquiring the artifact path failed for some reason
     */
    public static File resolveJarArtifact(final ArtifactCoordinates coordinates) throws IOException {
        return resolveArtifact(coordinates, "jar");
    }

    /**
     * Tries to find a maven jar artifact from the system property {@code "maven.repo.local"} This property is a list of
     * platform separated directory names.  If not specified, then it looks in {@code ${user.home}/.m2/repository} by default.
     * <p>
     * If it can't find it in local paths, then will try to download from a remote repository from the system property
     * {@code "remote.maven.repo"}.  There is no default remote repository.  It will download both the pom and jar and put it
     * into the first directory listed in {@code "maven.repo.local"} (or the default directory).  This directory will be
     * created if it doesn't exist.
     * <p>
     * Finally, if you do not want a message to console, then set the system property {@code "maven.download.message"} to
     * {@code "false"}.
     *
     * @param coordinates the non-{@code null} Maven coordinates object
     * @param packaging a non-{@code null} string with the exact packaging type desired (e.g. {@code pom}, {@code jar}, etc.)
     * @return the absolute path to the artifact, or {@code null} if none exists
     * @throws IOException if acquiring the artifact path failed for some reason
     */
    public static File resolveArtifact(final ArtifactCoordinates coordinates, final String packaging) throws IOException {
        String artifactRelativePath = coordinates.relativeArtifactPath(File.separatorChar);
        String artifactRelativeHttpPath = coordinates.relativeArtifactPath('/');
        String artifactRelativeMetadataHttpPath = coordinates.relativeMetadataPath('/');
        final MavenSettings settings = MavenSettings.getSettings();
        final Path localRepository = settings.getLocalRepository();
        final File localRepositoryFile = localRepository.toFile();

        final String pomPath = artifactRelativePath + ".pom";

        // serialize artifact lookup because we want to prevent parallel download
        synchronized (artifactLock) {
            if ("pom".equals(packaging)) {
                // ignore classifier
                Path fp = localRepository.resolve(pomPath);
                if (Files.exists(fp)) {
                    return fp.toFile();
                }
                List<String> remoteRepos = settings.getRemoteRepositories();
                if (remoteRepos.isEmpty()) {
                    return null;
                }
                final File pomFile = new File(localRepositoryFile, pomPath);
                for (String remoteRepository : remoteRepos) {
                    try {
                        String remotePomPath = remoteRepository + artifactRelativeHttpPath + ".pom";
                        downloadFile(coordinates + ":" + packaging, remotePomPath, pomFile);
                        if (pomFile.exists()) { //download successful
                            return pomFile;
                        }
                    } catch (IOException e) {
                        Module.getModuleLogger().trace(e, "Could not download '%s' from '%s' repository", artifactRelativePath, remoteRepository);
                        // try next one
                    }
                }
                if (coordinates.isSnapshot()) {
                    // Check repositories for timestamp snapshots
                    String timestampedArtifactRelativePath;
                    for (String remoteRepository : remoteRepos) {
                        try {
                            String remoteMetadataPath = remoteRepository + artifactRelativeMetadataHttpPath;
                            timestampedArtifactRelativePath = coordinates.relativeArtifactPath('/', downloadTimestampVersion(coordinates + ":" + packaging, remoteMetadataPath));
                            String remotePomPath = remoteRepository + timestampedArtifactRelativePath + ".pom";
                            downloadFile(coordinates + ":" + packaging, remotePomPath, pomFile);
                            if (pomFile.exists()) { //download successful
                                return pomFile;
                            }
                        } catch (IOException | XPathExpressionException e) {
                            Module.getModuleLogger().trace(e, "Could not download '%s' from '%s' repository", artifactRelativePath, remoteRepository);
                            // try next one
                        }
                    }
                }
            } else {
                final String coordinatesClassifier = coordinates.getClassifier();
                String classifier = coordinatesClassifier.isEmpty() ? "" : "-" + coordinatesClassifier;
                String artifactPath = artifactRelativePath + classifier + "." + packaging;
                Path fp = localRepository.resolve(artifactPath);
                if (Files.exists(fp)) {
                    return fp.toFile();
                }

                List<String> remoteRepos = settings.getRemoteRepositories();
                if (remoteRepos.isEmpty()) {
                    return null;
                }

                final File artifactFile = new File(localRepositoryFile, artifactPath);
                final File pomFile = new File(localRepositoryFile, pomPath);
                for (String remoteRepository : remoteRepos) {
                    try {
                        String remotePomPath = remoteRepository + artifactRelativeHttpPath + ".pom";
                        String remoteArtifactPath = remoteRepository + artifactRelativeHttpPath + classifier + "." + packaging;
                        downloadFile(coordinates + ":pom", remotePomPath, pomFile);
                        if (!pomFile.exists()) {
                            // no POM; skip it
                            continue;
                        }
                        downloadFile(coordinates + ":" + packaging, remoteArtifactPath, artifactFile);
                        if (artifactFile.exists()) { //download successful
                            return artifactFile;
                        }
                    } catch (IOException e) {
                        Module.getModuleLogger().trace(e, "Could not download '%s' from '%s' repository", artifactRelativePath, remoteRepository);
                        //
                    }
                }
                if (coordinates.isSnapshot()) {
                    String timestampedArtifactRelativePath;
                    for (String remoteRepository : remoteRepos) {
                        try {
                            String remoteMetadataPath = remoteRepository + artifactRelativeMetadataHttpPath;
                            timestampedArtifactRelativePath = coordinates.relativeArtifactPath('/', downloadTimestampVersion(coordinates + ":" + packaging, remoteMetadataPath));
                            String remotePomPath = remoteRepository + timestampedArtifactRelativePath + ".pom";
                            String remoteArtifactPath = remoteRepository + timestampedArtifactRelativePath + classifier + "." + packaging;
                            downloadFile(coordinates + ":pom", remotePomPath, pomFile);
                            if (!pomFile.exists()) {
                                // no POM; skip it
                                continue;
                            }
                            downloadFile(coordinates + ":" + packaging, remoteArtifactPath, artifactFile);
                            if (artifactFile.exists()) { //download successful
                                return artifactFile;
                            }
                        } catch (IOException | XPathExpressionException e) {
                            Module.getModuleLogger().trace(e, "Could not download '%s' from '%s' repository", artifactRelativePath, remoteRepository);
                            // try next one
                        }
                    }
                }
            }
            //could not find it in remote
            Module.getModuleLogger().trace("Could not find in any remote repository");
            return null;
        }
    }

    public static void downloadFile(String artifact, String src, File dest) throws IOException {
        if (dest.exists()) {
            return;
        }
        final URL url = new URL(src);
        final URLConnection connection = MavenSettings.getSettings().openConnection(url);
        boolean message = Boolean.getBoolean("maven.download.message");

        try (InputStream bis = connection.getInputStream()) {
            dest.getParentFile().mkdirs();
            if (message) {
                LOGGER.trace("Downloading " + artifact);
            }
            Files.copy(bis, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String downloadTimestampVersion(String artifact, String metadataSrc) throws IOException, XPathExpressionException {
        final URL url = new URL(metadataSrc);
        final URLConnection connection = MavenSettings.getSettings().openConnection(url);
        boolean message = Boolean.getBoolean("maven.download.message");

        try (InputStream is = connection.getInputStream()) {
            if (message) {
                LOGGER.trace("Downloading maven-metadata.xml for " + artifact);
            }
            return snapshotVersionXpath.evaluate(new InputSource(is));
        }
    }

    /**
     * A utility method to create a Maven artifact resource loader for the given artifact name.
     *
     * @param name the artifact name
     * @return the resource loader
     * @throws IOException if the artifact could not be resolved
     */
    public static ResourceLoader createMavenArtifactLoader(final String name) throws IOException {
        return createMavenArtifactLoader(MavenResolver.createDefaultResolver(), name);
    }

    /**
     * A utility method to create a Maven artifact resource loader for the given artifact name.
     *
     * @param mavenResolver the Maven resolver to use (must not be {@code null})
     * @param name the artifact name
     * @return the resource loader
     * @throws IOException if the artifact could not be resolved
     */
    public static ResourceLoader createMavenArtifactLoader(final MavenResolver mavenResolver, final String name) throws IOException {
        return createMavenArtifactLoader(mavenResolver, ArtifactCoordinates.fromString(name), name);
    }

    /**
     * A utility method to create a Maven artifact resource loader for the given artifact coordinates.
     *
     * @param mavenResolver the Maven resolver to use (must not be {@code null})
     * @param coordinates the artifact coordinates to use (must not be {@code null})
     * @param rootName the resource root name to use (must not be {@code null})
     * @return the resource loader
     * @throws IOException if the artifact could not be resolved
     */
    public static ResourceLoader createMavenArtifactLoader(final MavenResolver mavenResolver, final ArtifactCoordinates coordinates, final String rootName) throws IOException {
        File fp = mavenResolver.resolveJarArtifact(coordinates);
        if (fp == null) return null;
        JarFile jarFile = JDKSpecific.getJarFile(fp, true);
        return ResourceLoaders.createJarResourceLoader(rootName, jarFile);
    }

    static <T> T doIo(PrivilegedExceptionAction<T> action) throws IOException {
        try {
            return AccessController.doPrivileged(action);
        } catch (PrivilegedActionException e) {
            try {
                throw e.getCause();
            } catch (IOException | RuntimeException | Error e1) {
                throw e1;
            } catch (Throwable t) {
                throw new UndeclaredThrowableException(t);
            }
        }
    }

    static <T> T doIo(PrivilegedExceptionAction<T> action, AccessControlContext context) throws IOException {
        try {
            return AccessController.doPrivileged(action, context);
        } catch (PrivilegedActionException e) {
            try {
                throw e.getCause();
            } catch (IOException | RuntimeException | Error e1) {
                throw e1;
            } catch (Throwable t) {
                throw new UndeclaredThrowableException(t);
            }
        }
    }
}
