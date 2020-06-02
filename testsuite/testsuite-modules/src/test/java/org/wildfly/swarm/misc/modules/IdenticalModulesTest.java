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
import org.joox.Match;
import org.junit.Assert;
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

@SuppressWarnings("deprecation") // ModuleIdentifier is deprecated, but it conveys intent better than a plain String
public class IdenticalModulesTest {
    private static final Set<ModuleIdentifier> exceptions = new HashSet<>(Arrays.asList(
            // The `org.keycloak.keycloak-core` and `org.keycloak.keycloak-common` modules are defined
            // in 2 Keycloak feature packs: `keycloak-adapter-feature-pack` (used in Keycloak client fractions)
            // and `keycloak-server-feature-pack` (used in the Keycloak server fraction). The `module.xml` files
            // have the same meaning, but are unfortunately textually different.
            ModuleIdentifier.create("org.keycloak.keycloak-core"),
            ModuleIdentifier.create("org.keycloak.keycloak-common"),
            // known differences that are most likely wrong, but we're not sure how to fix them
            ModuleIdentifier.create("org.jboss.as.transactions"),
            ModuleIdentifier.create("org.jboss.jts"),
            ModuleIdentifier.create("org.hibernate.validator"),
            ModuleIdentifier.create("com.squareup.okhttp3"), // Keycloak server has a different module definition :-(
            ModuleIdentifier.create("org.apache.commons.lang") // Keycloak server has a different namespace :-(
    ));

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void allModuleXmlsAreIdenticalForSingleModule() throws IOException {
        SetMultimap<ModuleIdentifier, ModuleXmlContent> allModules = HashMultimap.create();
        SetMultimap<ModuleXmlContent, Path> allModuleXmls = HashMultimap.create();

        Path testsuite = Paths.get(System.getProperty("current.project.dir"));
        Path projectRoot = testsuite.getParent().getParent(); // depends on test suite directory structure
        assertTrue(Files.isRegularFile(projectRoot.resolve("pom.xml")));
        assertTrue(Files.isRegularFile(projectRoot.resolve("README.md")));
        assertTrue(Files.isDirectory(projectRoot.resolve("fractions")));
        assertTrue(Files.isDirectory(projectRoot.resolve("testsuite")));

        System.out.println("Scanning directory " + projectRoot);
        try (Stream<Path> files = Files.walk(projectRoot)) {
            files.filter(IdenticalModulesTest::isProcessedModuleXml)
                    .peek(System.out::println)
                    .forEach(moduleXmlPath -> {
                        try {
                            Match parsedXml = $(moduleXmlPath.toFile());
                            ModuleIdentifier moduleId = ModuleIdentifier.create(parsedXml.attr("name"),
                                    parsedXml.attr("slot"));

                            if (exceptions.contains(moduleId)) {
                                return;
                            }

                            ModuleXmlContent moduleXmlContent = new ModuleXmlContent(
                                    new String(Files.readAllBytes(moduleXmlPath), StandardCharsets.UTF_8));
                            allModules.put(moduleId, moduleXmlContent);
                            allModuleXmls.put(moduleXmlContent, moduleXmlPath);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        for (ModuleIdentifier moduleId : allModules.keySet()) {
            Set<ModuleXmlContent> moduleXmlContents = allModules.get(moduleId);
            if (moduleXmlContents.size() != 1) {
                int counter = 1;
                StringBuilder text = new StringBuilder();
                for (ModuleXmlContent moduleXmlContent : moduleXmlContents) {
                    text.append("variant ").append(counter).append(":\n");
                    for (Path moduleXml : allModuleXmls.get(moduleXmlContent)) {
                        text.append("     ").append(moduleXml.toString()).append("\n");
                    }
                    text.append("\n");
                    for (String line : moduleXmlContent.toString().split("\n")) {
                        text.append("     | ").append(line).append("\n");
                    }
                    counter++;
                }

                if (moduleXmlContents.size() == 2) {
                    Iterator<ModuleXmlContent> it = moduleXmlContents.iterator();
                    ModuleXmlContent first = it.next();
                    ModuleXmlContent second = it.next();
                    Diff diff = DiffBuilder.compare(Input.fromString(first.toString()))
                            .withTest(Input.fromString(second.toString()))
                            .checkForIdentical()
                            .build();
                    if (diff.hasDifferences()) {
                        text.append("found XML structural differences:\n");
                        for (Difference difference : diff.getDifferences()) {
                            for (String differenceLine : difference.toString().split("\n")) {
                                text.append("     | ").append(differenceLine).append("\n");
                            }
                            text.append("     -----\n");
                        }
                        text.delete(text.length() - "     -----\n".length(), text.length()); // remove last separator
                    } else {
                        text.append("     ----- no XML structural difference found, why are there textual differences? -----\n");
                    }
                } else {
                    text.append("     ----- number of different files is != 2, not performing XML structural diff -----\n");
                }

                collector.addError(new AssertionError("Module " + moduleId + " is ambiguous:\n" + text));
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

    private static final class ModuleXmlContent {
        private final String value;

        public ModuleXmlContent(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModuleXmlContent)) return false;
            ModuleXmlContent that = (ModuleXmlContent) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
