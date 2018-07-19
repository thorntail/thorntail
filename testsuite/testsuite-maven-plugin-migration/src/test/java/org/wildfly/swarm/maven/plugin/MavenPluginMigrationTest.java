/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.maven.plugin;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class MavenPluginMigrationTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Verifier verifier;

    private Path logPath;

    @Before
    public void setUpProject() throws IOException, VerificationException {
        assumeTrue("Set the M2_HOME environment variable", System.getenv("M2_HOME") != null);
        assumeTrue("Run from Maven or set the project.version system property", System.getProperty("project.version") != null);

        File projectDir = ResourceExtractor.extractResourcePath(getClass(), "/testing-project", tmp.getRoot(), true);

        verifier = new Verifier(projectDir.getAbsolutePath(), true);
        verifier.setForkJvm(true);

        String settingsXml = System.getProperty("org.apache.maven.user-settings");
        if (settingsXml != null && new File(settingsXml).isFile()) {
            verifier.addCliOption("-s");
            verifier.addCliOption(settingsXml);
        }

        logPath = Paths.get(verifier.getBasedir()).resolve(verifier.getLogFileName());
    }

    @Test
    public void migrationTest() {
        try {
            migrateProjectFromWildFlySwarmToThorntail();
            verifyMigratedPomXml();
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private void migrateProjectFromWildFlySwarmToThorntail() throws IOException, VerificationException {
        String thorntailVersion = System.getProperty("project.version");
        String goal = "io.thorntail:thorntail-maven-plugin:" + thorntailVersion + ":migrate-from-wildfly-swarm";

        verifier.setSystemProperty("targetVersion", "2.0.0.Final"); // hardcoded in expected-pom.xml
        verifier.executeGoal(goal);

        String log = new String(Files.readAllBytes(logPath), StandardCharsets.UTF_8);

        assertThat(log).contains("Upgrading to Thorntail 2.0.0.Final");
        assertThat(log).contains(slashes("./pom.xml: migrate dependency exclusion on org.wildfly.swarm:cdi-config at dependency org.wildfly.swarm:cdi"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency exclusion on org.wildfly.swarm:datasources at dependency org.wildfly.swarm:jpa of pluginManagement org.wildfly.swarm:wildfly-swarm-plugin in profile my-profile"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency exclusion on org.wildfly.swarm:spi at dependency org.wildfly.swarm:management-console of plugin org.apache.maven.plugins:maven-failsafe-plugin"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency org.wildfly.swarm:cdi (target version: 2.0.0.Final)"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency org.wildfly.swarm:jaxrs (target version: null)"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency org.wildfly.swarm:jpa of pluginManagement org.wildfly.swarm:wildfly-swarm-plugin in profile my-profile (target version: null)"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency org.wildfly.swarm:jsf in profile my-profile (target version: null)"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency org.wildfly.swarm:logging of plugin org.wildfly.swarm:wildfly-swarm-plugin in profile my-profile (target version: 2.0.0.Final)"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency org.wildfly.swarm:management of plugin org.apache.maven.plugins:maven-failsafe-plugin (target version: null)"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency org.wildfly.swarm:management-console of plugin org.apache.maven.plugins:maven-failsafe-plugin (target version: 2.0.0.Final)"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependency org.wildfly.swarm:undertow (target version: null)"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependencyManagement org.wildfly.swarm:bean-validation in profile my-profile (target version: 2.0.0.Final)"));
        assertThat(log).contains(slashes("./pom.xml: migrate dependencyManagement org.wildfly.swarm:bom (target version: null)"));
        assertThat(log).contains(slashes("./pom.xml: migrate plugin org.wildfly.swarm:wildfly-swarm-plugin (target version: null)"));
        assertThat(log).contains(slashes("./pom.xml: migrate plugin org.wildfly.swarm:wildfly-swarm-plugin in profile my-profile (target version: null)"));
        assertThat(log).contains(slashes("./pom.xml: migrate pluginManagement org.wildfly.swarm:wildfly-swarm-plugin in profile my-profile (target version: null)"));
        assertThat(log).contains("migrate version property version.org.wildfly.swarm to value 2.0.0.Final");
        assertThat(log).contains("migrate version property version.wildfly-swarm to value 2.0.0.Final");

        assertThat(log).doesNotContain("[ERROR]");
        assertThat(log).doesNotContain("[WARNING]");
        assertThat(log).contains("BUILD SUCCESS");
    }

    private void verifyMigratedPomXml() {
        Path projectDir = Paths.get(verifier.getBasedir());
        Diff diff = DiffBuilder.compare(Input.fromFile(projectDir.resolve("expected-pom.xml").toFile()))
                .withTest(Input.fromFile(projectDir.resolve("pom.xml").toFile()))
                .checkForIdentical()
                .build();
        assertThat(diff.hasDifferences())
                .as(diff.getDifferences().toString())
                .isFalse();
    }

    @After
    public void tearDown() {
        if (verifier != null) {
            verifier.resetStreams();
        }
    }

    private static String slashes(String migrationMessage) {
        int pathEndIndex = migrationMessage.indexOf(':');
        String path = migrationMessage.substring(0, pathEndIndex);
        path = path.replace('/', File.separatorChar);
        return path + migrationMessage.substring(pathEndIndex);
    }
}
