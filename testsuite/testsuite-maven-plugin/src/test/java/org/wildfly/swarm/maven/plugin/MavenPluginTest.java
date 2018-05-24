/**
 * Copyright 2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.maven.plugin;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * End-to-end tests of the Swarm Maven plugin.
 *
 * <p>The Swarm build tools, and consequently the Swarm Maven plugin, have a lot of features that interact
 * in subtle ways. Some of these interactions don't make a lot of sense, but they aren't prevented in any way.
 * So these tests "codify" the existing behavior, because it is something users tend to rely on even if that
 * was never the intent.</p>
 *
 * <p>The tests can be run in one of three ways:</p>
 *
 * <ul>
 *     <li>default -- only a small number of hand-selected tests will run, to finish quickly</li>
 *     <li>full matrix -- runs the entire testing matrix, which takes a <b>lot</b> of time;
 *         this mode is enabled by setting system property {@code swarm.test.full}</li>
 *     <li>single combination -- runs a single test defined by a system property {@code swarm.test.maven.plugin.single};
 *         the directory with the testing project will be preserved for manual inspection</li>
 * </ul>
 *
 * <p>Currently, there's a couple of known issues, so some of the tests are commented out.
 * That is expected to change over time.</p>
 */
@RunWith(Parameterized.class)
public class MavenPluginTest {
    private static final String RUN_FULL_MATRIX_KEY = "swarm.test.full";
    private static final String SINGLE_TESTING_PROJECT_KEY = "swarm.test.maven.plugin.single";

    @Parameters(name = "{0}")
    public static Iterable<?> parameters() {
        String singleTestingProject = System.getProperty(SINGLE_TESTING_PROJECT_KEY);
        if (singleTestingProject != null) {
            return Collections.singleton(TestingProject.deserialize(singleTestingProject));
        }

        boolean runFullMatrix = System.getProperty(RUN_FULL_MATRIX_KEY) != null;

        if (!runFullMatrix) {
            return Arrays.asList(
                    new TestingProject(Packaging.WAR, Dependencies.FRACTIONS, Autodetection.FORCE,
                                       new IncludedTechnology[]{IncludedTechnology.SERVLET, IncludedTechnology.JAX_RS},
                                       AdditionalDependency.NON_JAVA_EE, AdditionalFraction.NONE),
                    // SWARM-870
/*
                    new TestingProject(Packaging.WAR, Dependencies.JAVA_EE_APIS, Autodetection.FORCE,
                                       new IncludedTechnology[]{IncludedTechnology.SERVLET},
                                       AdditionalDependency.NON_JAVA_EE, AdditionalFraction.ALREADY_PRESENT),
*/
                    // SWARM-814
                    new TestingProject(Packaging.WAR, Dependencies.JAVA_EE_APIS, Autodetection.WHEN_MISSING,
                                       new IncludedTechnology[]{IncludedTechnology.SERVLET, IncludedTechnology.EJB},
                                       AdditionalDependency.NONE, AdditionalFraction.NONE),
                    // SWARM-970
/*
                    new TestingProject(Packaging.WAR, Dependencies.FRACTIONS, Autodetection.FORCE,
                                       new IncludedTechnology[]{IncludedTechnology.SERVLET, IncludedTechnology.EJB},
                                       AdditionalDependency.NONE, AdditionalFraction.NONE),
*/
                    new TestingProject(Packaging.WAR_WITH_MAIN, Dependencies.FRACTIONS, Autodetection.NEVER,
                                       new IncludedTechnology[]{IncludedTechnology.SERVLET, IncludedTechnology.JAX_RS},
                                       AdditionalDependency.NON_JAVA_EE, AdditionalFraction.NOT_YET_PRESENT),
                    new TestingProject(Packaging.WAR_WITH_MAIN, Dependencies.JAVA_EE_APIS, Autodetection.WHEN_MISSING,
                                       new IncludedTechnology[]{IncludedTechnology.SERVLET, IncludedTechnology.JAX_RS},
                                       AdditionalDependency.USING_JAVA_EE, AdditionalFraction.NONE),
                    // SWARM-869
/*
                    new TestingProject(Packaging.JAR_WITH_MAIN, Dependencies.FRACTIONS, Autodetection.WHEN_MISSING,
                                       new IncludedTechnology[]{IncludedTechnology.SERVLET, IncludedTechnology.JAX_RS},
                                       AdditionalDependency.USING_JAVA_EE, AdditionalFraction.NOT_YET_PRESENT),
*/
                    new TestingProject(Packaging.JAR_WITH_MAIN, Dependencies.JAVA_EE_APIS, Autodetection.FORCE,
                                       new IncludedTechnology[]{IncludedTechnology.SERVLET, IncludedTechnology.JAX_RS},
                                       AdditionalDependency.NONE, AdditionalFraction.ALREADY_PRESENT)
            );
        }

        List<TestingProject> testingProjects = new ArrayList<>();
        for (Packaging packaging : Packaging.values()) {
            for (Dependencies dependencies : Dependencies.values()) {
                for (Autodetection autodetection : Autodetection.values()) {
                    for (AdditionalDependency additionalDependency : AdditionalDependency.values()) {
                        for (AdditionalFraction additionalFraction : AdditionalFraction.values()) {
                            // a lot of these fail because of SWARM-869
                            if (autodetection == Autodetection.WHEN_MISSING && additionalFraction != AdditionalFraction.NONE) {
                                continue;
                            }
                            // a lot of these fail because of SWARM-870
                            if (autodetection != Autodetection.NEVER && additionalFraction == AdditionalFraction.ALREADY_PRESENT) {
                                continue;
                            }

                            testingProjects.add(new TestingProject(
                                    packaging, dependencies, autodetection,
                                    new IncludedTechnology[]{IncludedTechnology.SERVLET},
                                    additionalDependency, additionalFraction
                            ));
                            testingProjects.add(new TestingProject(
                                    packaging, dependencies, autodetection,
                                    new IncludedTechnology[]{IncludedTechnology.SERVLET, IncludedTechnology.JAX_RS},
                                    additionalDependency, additionalFraction
                            ));

                            // a lot of these fail because of SWARM-970
                            if (dependencies == Dependencies.FRACTIONS && autodetection == Autodetection.FORCE) {
                                continue;
                            }
                            testingProjects.add(new TestingProject(
                                    packaging, dependencies, autodetection,
                                    new IncludedTechnology[]{IncludedTechnology.SERVLET, IncludedTechnology.EJB},
                                    additionalDependency, additionalFraction
                            ));
                        }
                    }
                }
            }
        }
        return testingProjects;
    }

    @Parameter
    public TestingProject testingProject;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder() {
        @Override
        protected void after() {
            if (System.getProperty(SINGLE_TESTING_PROJECT_KEY) != null) {
                // the test is run manually for a single combination
                // don't delete the testing project directory, it's likely to be inspected manually
                return;
            }

            super.after();
        }
    };

    private Verifier verifier;

    private Path logPath;

    @Before
    public void setUpProject() throws IOException, VerificationException {
        assumeTrue("Set the M2_HOME environment variable", System.getenv("M2_HOME") != null);
        assumeTrue("Run from Maven or set the project.version system property", System.getProperty("project.version") != null);

        File projectDir = ResourceExtractor.extractResourcePath(getClass(), "/testing-project", tmp.getRoot(), true);
        testingProject.prepare(projectDir.toPath());

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
    public void buildUberjarAndRunTests() {
        try {
            doBuildUberjarAndRunTests();
        } catch (Exception | AssertionError e) {
            String additionalMessage;
            if (System.getProperty(SINGLE_TESTING_PROJECT_KEY) == null) {
                additionalMessage = "Test failed for project [" + testingProject + "], use -D" + SINGLE_TESTING_PROJECT_KEY
                        + "=" + testingProject.serialize() + " to run the test with this single combination again";
            } else {
                additionalMessage = "Test failed for project [" + testingProject + "], the testing project is kept in "
                        + verifier.getBasedir() + " for manual inspection";
            }

            throw new AssertionError(e.getMessage() + "\n\n" + additionalMessage, e);
        }
    }

    public void doBuildUberjarAndRunTests() throws IOException, VerificationException, InterruptedException {
        String goal = "package";
        if (testingProject.canRunTests()) {
            goal = "verify";

            // a lot of these fail because of SWARM-873
            if (testingProject.additionalDependency == AdditionalDependency.USING_JAVA_EE) {
                goal = "package";
            }
        }

        try {
            verifier.executeGoal(goal);
        } catch (VerificationException e) {
            if (testingProject.dependencies == Dependencies.JAVA_EE_APIS
                    && testingProject.autodetection == Autodetection.NEVER) {
                // the only situation when build failure is expected
                String log = new String(Files.readAllBytes(logPath), StandardCharsets.UTF_8);
                assertThat(log).contains("No WildFly Swarm Bootstrap fraction found");
                return;
            }

            throw e;
        }

        verifier.assertFilePresent("target/testing-project." + testingProject.packaging.fileExtension());
        verifier.assertFilePresent("target/testing-project-swarm.jar");

        String log = new String(Files.readAllBytes(logPath), StandardCharsets.UTF_8);

        assertThat(log).doesNotContain("[ERROR]");
        if (testingProject.packaging.hasCustomMain()) {
            int count = 0;
            int index = 0;
            while ((index = log.indexOf("[WARNING]", index)) != -1) {
                count++;
                index++;
            }

            assertThat(log).contains("Custom main() usage is intended to be deprecated in a future release");
            // 1st warning for wildfly-swarm:package
            // 2nd warning possibly for wildfly-swarm:start for tests
            // 3rd warning possibly for wildfly-swarm:stop for tests
            assertThat(count).as("There should only be 1 to 3 warnings").isIn(1, 2, 3);
        } else {
            assertThat(log).doesNotContain("[WARNING]");
        }

        assertThat(log).contains("BUILD SUCCESS");

        checkFractionAutodetection(log);

        File uberjarFile = new File(verifier.getBasedir(), "target/testing-project-swarm.jar");
        Archive uberjar = ShrinkWrap.createFromZipFile(GenericArchive.class, uberjarFile);

        checkFractionsPresent(uberjar);

        checkMainClass(uberjar);
    }

    private void checkFractionAutodetection(String log) {
        if (testingProject.doesAutodetectionHappen()) {
            assertThat(log).contains("Scanning for needed WildFly Swarm fractions");
        } else {
            assertThat(log).doesNotContain("Scanning for needed WildFly Swarm fractions");
        }
    }

    private void checkFractionsPresent(Archive uberjar) throws IOException {
        assertThat(uberjar.contains("META-INF/wildfly-swarm-manifest.yaml")).isTrue();

        String manifestContent = readFileFromArchive(uberjar, "META-INF/wildfly-swarm-manifest.yaml");

        for (String fraction : testingProject.fractionsThatShouldBePresent()) {
            // module name
        	assertThat(manifestContent).contains("org.wildfly.swarm." + fraction);
            // bootstrap artifact groupId
            assertThat(manifestContent).contains("io.thorntail:" + fraction);
            // maven gav
            assertThat(uberjar.contains("m2repo/io/thorntail/" + fraction)).isTrue();
        }

        for (String fraction : testingProject.fractionsThatShouldBeMissing()) {
            assertThat(manifestContent).doesNotContain("org.wildfly.swarm." + fraction);
            assertThat(manifestContent).doesNotContain("io.thorntail:" + fraction);
            assertThat(uberjar.contains("m2repo/io/thorntail/" + fraction)).isFalse();
        }
    }

    private void checkMainClass(Archive uberjar) throws IOException {
        String javaManifest = readFileFromArchive(uberjar, "META-INF/MANIFEST.MF");
        assertThat(javaManifest).contains("Main-Class");
        assertThat(javaManifest).doesNotContain("org.wildfly.swarm.test.Main");

        String swarmManifest = readFileFromArchive(uberjar, "META-INF/wildfly-swarm-manifest.yaml");
        if (testingProject.packaging.hasCustomMain()) {
            assertThat(swarmManifest).contains("main-class: org.wildfly.swarm.test.Main");
        } else {
            assertThat(swarmManifest).doesNotContain("main-class: org.wildfly.swarm.test.Main");
        }
    }

    @After
    public void tearDown() throws InterruptedException {
        if (verifier != null) {
            verifier.resetStreams();
        }
    }

    // ---

    private static String readFileFromArchive(Archive archive, String path) throws IOException {
        try (InputStream manifest = archive.get(path).getAsset().openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(manifest, StandardCharsets.UTF_8));
            return reader.lines().collect(Collectors.joining());
        }
    }
}
