/**
 * Copyright 2019 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.misc.modules;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.maven.ArtifactCoordinates;
import org.joox.Match;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.joox.JOOX.$;
import static org.junit.Assert.assertTrue;

public class DependencyConvergenceInModulesTest {
    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void artifactVersionsInModuleXmlsConverge() throws IOException {
        SetMultimap<Coordinates, VersionedCoordinates> allArtifacts = HashMultimap.create();
        SetMultimap<VersionedCoordinates, Path> allModuleXmls = HashMultimap.create();

        Path testsuite = Paths.get(System.getProperty("current.project.dir"));
        Path projectRoot = testsuite.getParent().getParent(); // depends on test suite directory structure
        assertTrue(Files.isRegularFile(projectRoot.resolve("pom.xml")));
        assertTrue(Files.isRegularFile(projectRoot.resolve("README.md")));
        assertTrue(Files.isDirectory(projectRoot.resolve("fractions")));
        assertTrue(Files.isDirectory(projectRoot.resolve("testsuite")));

        System.out.println("Scanning directory " + projectRoot);
        try (Stream<Path> files = Files.walk(projectRoot)) {
            files.filter(DependencyConvergenceInModulesTest::isProcessedModuleXml)
                    .peek(System.out::println)
                    .forEach(moduleXmlPath -> {
                        try {
                            $(moduleXmlPath.toFile()).find("module resources artifact").each(artifact -> {
                                VersionedCoordinates gav = VersionedCoordinates.parse($(artifact).attr("name"));
                                allArtifacts.put(gav.withoutVersion(), gav);
                                allModuleXmls.put(gav, moduleXmlPath);
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        for (Coordinates ga : allArtifacts.keySet()) {
            Set<VersionedCoordinates> allGavs = allArtifacts.get(ga);
            if (allGavs.size() != 1) {
                StringBuilder text = new StringBuilder();
                for (VersionedCoordinates gav : allGavs) {
                    text.append("version ").append(gav.version()).append(" coming from:\n");
                    for (Path moduleXml : allModuleXmls.get(gav)) {
                        text.append("- ").append(moduleXml).append("\n");
                    }
                }

                collector.addError(new AssertionError("Duplicate artifact " + ga + ":\n" + text));
            }
        }
    }

    private static boolean isProcessedModuleXml(Path path) {
        if (path.getFileName().toString().equals("module.xml")) {
            String pathStr = path.toString();
            return pathStr.contains("target")
                    && !pathStr.contains("depSources")
                    && !pathStr.contains("test-classes")
                    && !pathStr.contains("testsuite");
        }

        return false;
    }

    public static final class Coordinates {
        private final String groupId;
        private final String artifactId;

        public static Coordinates parse(String coords) {
            String[] parts = coords.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Expected 'groupId:artifactId': " + coords);
            }

            return new Coordinates(parts[0], parts[1]);
        }

        public Coordinates(String groupId, String artifactId) {
            this.groupId = Objects.requireNonNull(groupId, "Group ID must be set");
            this.artifactId = Objects.requireNonNull(artifactId, "Artifact ID must be set");
        }

        public String groupId() {
            return groupId;
        }

        public String artifactId() {
            return artifactId;
        }

        @Override
        public String toString() {
            return groupId + ":" + artifactId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Coordinates)) return false;
            Coordinates that = (Coordinates) o;
            return Objects.equals(groupId, that.groupId) &&
                    Objects.equals(artifactId, that.artifactId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, artifactId);
        }
    }

    public static final class VersionedCoordinates {
        private final Coordinates coordinates;
        private final String version;

        public static VersionedCoordinates parse(String coords) {
            String[] parts = coords.split(":");
            // can also be groupId:artifactId:version:classifier, but we can ignore the classifier here
            if (parts.length != 3 && parts.length != 4) {
                throw new IllegalArgumentException("Expected 'groupId:artifactId:version': " + coords);
            }

            return new VersionedCoordinates(parts[0], parts[1], parts[2]);
        }

        public VersionedCoordinates(String groupId, String artifactId, String version) {
            this.coordinates = new Coordinates(groupId, artifactId);
            this.version = Objects.requireNonNull(version, "Version must be set");
        }

        public Coordinates withoutVersion() {
            return coordinates;
        }

        public String groupId() {
            return coordinates.groupId();
        }

        public String artifactId() {
            return coordinates.artifactId();
        }

        public String version() {
            return version;
        }

        @Override
        public String toString() {
            return coordinates + ":" + version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VersionedCoordinates)) return false;
            VersionedCoordinates that = (VersionedCoordinates) o;
            return Objects.equals(coordinates, that.coordinates) &&
                    Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(coordinates, version);
        }
    }
}
