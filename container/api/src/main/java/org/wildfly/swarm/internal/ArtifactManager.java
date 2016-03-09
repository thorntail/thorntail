/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.maven.ArtifactCoordinates;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.bootstrap.modules.MavenResolvers;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;
import org.wildfly.swarm.spi.api.ArtifactLookup;

/**
 * @author Bob McWhirter
 */
public class ArtifactManager implements ArtifactLookup {

    public ArtifactManager(WildFlySwarmDependenciesConf deps) {
        this.deps = deps;
    }

    public ArtifactManager(InputStream in) throws IOException {
        this.deps = new WildFlySwarmDependenciesConf(in);
    }

    public ArtifactManager() throws IOException {
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(WildFlySwarmDependenciesConf.CLASSPATH_LOCATION);
        if (in != null) {
            this.deps = new WildFlySwarmDependenciesConf(in);
        } else {
            this.deps = null;
        }
    }

    public JavaArchive artifact(String gav) throws IOException, ModuleLoadException {
        return artifact(gav, null);
    }

    public JavaArchive artifact(String gav, String asName) throws IOException, ModuleLoadException {
        final File file = findFile(gav);

        if (file == null) {
            throw new RuntimeException("Artifact not found.");
        }

        return ShrinkWrap.create(ZipImporter.class, asName == null ? file.getName() : asName)
                .importFrom(file)
                .as(JavaArchive.class);
    }

    public List<JavaArchive> allArtifacts() throws IOException {
        final List<JavaArchive> archives = new ArrayList<>();

        if (this.deps != null) {
            for (MavenArtifactDescriptor each : this.deps.getPrimaryDependencies()) {
                final File artifact = MavenResolvers.get().resolveJarArtifact(each.mscCoordinates());
                archives.add(ShrinkWrap.create(ZipImporter.class, artifact.getName())
                                     .importFrom(artifact)
                                     .as(JavaArchive.class));
            }
        } else {
            final String classpath = System.getProperty("java.class.path");
            final String javaHome = System.getProperty("java.home");
            final Path pwd = Paths.get(System.getProperty("user.dir"));
            if (classpath != null) {
                for (final String element : classpath.split(File.pathSeparator)) {
                    if (!element.startsWith(javaHome)) {
                        final File artifact = new File(element);

                        if (artifact.toPath().startsWith(pwd)) {

                            continue;
                        }

                        if (artifact.isFile()) {
                            archives.add(ShrinkWrap.create(ZipImporter.class, artifact.getName())
                                                 .importFrom(artifact)
                                                 .as(JavaArchive.class));
                        } else {
                            archives.add(ShrinkWrap.create(ExplodedImporter.class)
                                                 .importDirectory(artifact)
                                                 .as(JavaArchive.class));
                        }
                    }
                }
            }

        }

        return archives;
    }

    private File findFile(String gav) throws IOException, ModuleLoadException {

        // groupId:artifactId
        // groupId:artifactId:version
        // groupId:artifactId:packaging:version
        // groupId:artifactId:packaging:version:classifier

        String[] parts = gav.split(":");

        if (parts.length < 2) {
            throw new RuntimeException("GAV must includes at least 2 segments");
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String packaging = "jar";
        String version = null;
        String classifier = "";

        if (parts.length == 3) {
            version = parts[2];
        }

        if (parts.length == 4) {
            packaging = parts[2];
            version = parts[3];
        }

        if (parts.length == 5) {
            packaging = parts[2];
            version = parts[3];
            classifier = parts[4];
        }

        if (version != null && (version.isEmpty() || version.equals("*"))) {
            version = null;
        }

        if (version == null) {
            version = determineVersionViaDependenciesConf(groupId, artifactId, packaging, classifier);
        }

        if (version == null) {
            version = determineVersionViaClasspath(groupId, artifactId, packaging, classifier);
        }

        if (version == null) {
            throw new RuntimeException("Unable to determine version number from GAV: " + gav);
        }

        return MavenResolvers.get().resolveArtifact(
                new ArtifactCoordinates(
                        groupId,
                        artifactId,
                        version,
                        classifier == null ? "" : classifier), packaging);
    }

    String determineVersionViaDependenciesConf(String groupId, String artifactId, String packaging, String classifier) throws IOException {
        if (this.deps != null) {
            MavenArtifactDescriptor found = this.deps.find(groupId, artifactId, packaging, classifier);
            if (found != null) {

                return found.version();
            }
        }

        return null;
    }

    private String determineVersionViaClasspath(String groupId, String artifactId, String packaging, String classifier) {

        String regexp = ".*" + artifactId + "-(.+)" + (classifier.length() == 0 ? "" : "-" + classifier) + "." + packaging;
        Pattern pattern = Pattern.compile(regexp);

        for (final String element : System.getProperty("java.class.path").split(File.pathSeparator)) {
            Matcher matcher = pattern.matcher(element);
            if (matcher.matches()) {

                return matcher.group(1);
            }
        }

        return null;
    }

    final private WildFlySwarmDependenciesConf deps;

}
