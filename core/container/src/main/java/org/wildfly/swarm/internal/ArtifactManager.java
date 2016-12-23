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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.Vetoed;

import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.maven.ArtifactCoordinates;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.modules.MavenResolvers;
import org.wildfly.swarm.spi.api.ArtifactLookup;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Vetoed
public class ArtifactManager implements ArtifactLookup {

    public ArtifactManager() {
    }

    public JavaArchive artifact(String gav) throws IOException, ModuleLoadException {
        return artifact(gav, null);
    }

    public JavaArchive artifact(String gav, String asName) throws IOException, ModuleLoadException {
        final File file = findFile(gav);

        if (file == null) {
            throw SwarmMessages.MESSAGES.artifactNotFound(gav);
        }

        return ShrinkWrap.create(ZipImporter.class, asName == null ? file.getName() : asName)
                .importFrom(file)
                .as(JavaArchive.class);
    }

    public List<JavaArchive> allArtifacts() throws IOException {
        return allArtifacts("");
    }

    @Override
    public List<JavaArchive> allArtifacts(String... groupIdExclusions) throws IOException {
        Map<String, JavaArchive> archives = new HashMap<>();
        Set<String> archivesPaths;

        final List<String> exclusions = Arrays.asList(groupIdExclusions);

        ApplicationEnvironment env = ApplicationEnvironment.get();
        archivesPaths = env.resolveDependencies(exclusions);

        // package the shrinkwrap bits
        for (final String element : archivesPaths) {

            final File artifact = new File(element);

            if (artifact.isFile()) {
                archives.put(artifact.getName(), ShrinkWrap.create(ZipImporter.class, artifact.getName())
                        .importFrom(artifact)
                        .as(JavaArchive.class));
            } else {

                final String archiveName = FileSystemLayout.archiveNameForClassesDir(artifact.toPath());

                // pack resources and classes of the same project into one archive
                if (archives.containsKey(archiveName)) {
                    archives.get(archiveName).as(ExplodedImporter.class).importDirectory(artifact);
                } else {
                    archives.put(archiveName, ShrinkWrap.create(ExplodedImporter.class, archiveName)
                            .importDirectory(artifact)
                            .as(JavaArchive.class));
                }
            }
        }

        return new ArrayList<>(archives.values());
    }

    private File findFile(String gav) throws IOException, ModuleLoadException {

        // groupId:artifactId
        // groupId:artifactId:version
        // groupId:artifactId:packaging:version
        // groupId:artifactId:packaging:classifier:version

        String[] parts = gav.split(":");

        if (parts.length < 2) {
            throw SwarmMessages.MESSAGES.gavMinimumSegments();
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
            classifier = parts[3];
            version = parts[4];
        }

        if (version != null && (version.isEmpty() || version.equals("*"))) {
            version = null;
        }

        if (version == null) {
            version = determineVersionViaApplicationEnvironment(groupId, artifactId, packaging, classifier);
        }

        if (version == null) {
            version = determineVersionViaClasspath(groupId, artifactId, packaging, classifier);
        }

        if (version == null) {
            throw SwarmMessages.MESSAGES.unableToDetermineVersion(gav);
        }
        ArtifactCoordinates coords= new ArtifactCoordinates(
                groupId,
                artifactId,
                version,
                classifier == null ? "" : classifier);

        return MavenResolvers.get().resolveArtifact( coords, packaging );
    }


    String determineVersionViaApplicationEnvironment(String groupId, String artifactId, String packaging, String classifier) throws IOException {
        ApplicationEnvironment env = ApplicationEnvironment.get();

        if ( classifier.isEmpty() ) {
            classifier = null;
        }

        for (String dep : env.getDependencies()) {
            String[] parts = dep.split(":");

            String depGroupId = parts[0];
            String depArtifactId = parts[1];
            String depPackaging = parts[2];
            String depVersion = null;
            String depClassifier = null;
            if ( parts.length == 4 ) {
                depVersion = parts[3];
            } else {
                depClassifier = parts[3];
                depVersion = parts[4];
            }

            if ( groupId.equals( depGroupId ) ) {
                if ( artifactId.equals( depArtifactId ) ) {
                    if ( packaging.equals( depPackaging ) ) {
                        if ( classifier == null ) {
                            if ( depClassifier == null ) {
                                return depVersion;
                            }
                        } else {
                            if ( depClassifier != null && classifier.equals( depClassifier ) ) {
                                return depVersion;
                            }
                        }
                    }
                }
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

    //final private WildFlySwarmDependenciesConf deps;

}
