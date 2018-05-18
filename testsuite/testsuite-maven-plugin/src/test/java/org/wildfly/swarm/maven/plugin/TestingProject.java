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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds the testing Maven project setup and is able to prepare the {@code testing-project} directory to the desired state.
 * This means filling the blanks in {@code pom.xml}, ensuring that only one {@code web.xml} exists in the project,
 * adjusting {@code Main.java} and making sure that excluded {@code .java} files are deleted.
 *
 * @see Packaging
 * @see Dependencies
 * @see Autodetection
 * @see IncludedTechnology
 * @see AdditionalDependency
 * @see AdditionalFraction
 */
public final class TestingProject {
    public final Packaging packaging;

    public final Dependencies dependencies;

    public final Autodetection autodetection;

    public final Set<IncludedTechnology> includedTechnologies;

    public final AdditionalDependency additionalDependency;

    public final AdditionalFraction additionalFraction;

    public TestingProject(Packaging packaging, Dependencies dependencies, Autodetection autodetection,
                          IncludedTechnology[] includedTechnologies, AdditionalDependency additionalDependency,
                          AdditionalFraction additionalFraction) {
        this.packaging = packaging;
        this.dependencies = dependencies;
        this.autodetection = autodetection;
        Set<IncludedTechnology> techs = new HashSet<>(Arrays.asList(includedTechnologies));
        // to ensure that the test is well-formed: the intended set of included technologies must respect fraction dependencies
        for (IncludedTechnology includedTechnology : includedTechnologies) {
            if (!techs.containsAll(includedTechnology.dependsOn())) {
                throw new IllegalArgumentException("If you want to include " + includedTechnology
                        + ", you also have to include " + includedTechnology.dependsOn());
            }
        }
        if (packaging == Packaging.JAR_WITH_MAIN && !techs.contains(IncludedTechnology.SERVLET)) {
            throw new IllegalArgumentException("Project with JAR packaging needs SERVLET for the custom Main class");
        }
        this.includedTechnologies = Collections.unmodifiableSet(techs);
        this.additionalDependency = additionalDependency;
        this.additionalFraction = additionalFraction;
    }

    public void prepare(Path dir) throws IOException {
        String swarmVersion = System.getProperty("project.version"); // from Maven

        // pom.xml
        Path pomXml = dir.resolve("pom.xml");
        String pomXmlContent = new String(Files.readAllBytes(pomXml), StandardCharsets.UTF_8)
                .replace("<!--PLACEHOLDER:swarm-version-->", swarmVersion)
                .replace("<!--PLACEHOLDER:packaging-->", packaging.packagingType())
                .replace("<!--PLACEHOLDER:dependencies-->", dependenciesSnippet(swarmVersion))
                .replace("<!--PLACEHOLDER:configuration-->", swarmPluginConfigurationSnippet());
        Files.write(pomXml, pomXmlContent.getBytes(StandardCharsets.UTF_8));

        // web.xml
        if (packaging == Packaging.WAR || packaging == Packaging.WAR_WITH_MAIN) {
            Files.delete(dir.resolve(Paths.get("src", "main", "resources", "web.xml")));
            Files.delete(dir.resolve(Paths.get("src", "main", "resources")));
        } else if (packaging == Packaging.JAR_WITH_MAIN) {
            Files.delete(dir.resolve(Paths.get("src", "main", "webapp", "WEB-INF", "web.xml")));
            Files.delete(dir.resolve(Paths.get("src", "main", "webapp", "WEB-INF")));
            Files.delete(dir.resolve(Paths.get("src", "main", "webapp")));
        } else {
            throw new IllegalStateException("Unknown packaging " + packaging);
        }

        Path javaFiles = dir.resolve(Paths.get("src", "main", "java", "org", "wildfly", "swarm", "test"));
        Path testJavaFiles = dir.resolve(Paths.get("src", "test", "java", "org", "wildfly", "swarm", "test"));

        // Main.java
        Path mainClass = javaFiles.resolve("Main.java");
        if (this.packaging.hasCustomMain()) {
            String mainClassContent = new String(Files.readAllBytes(mainClass), StandardCharsets.UTF_8);

            for (Packaging packaging : Packaging.values()) {
                // only retain one section /*BEGIN:custom main:<packaging>*/ ... /*END:custom main:<packaging>*/;
                // the one that corresponds to the project's packaging
                if (this.packaging == packaging) {
                    continue;
                }

                String beginning = Pattern.quote("/*BEGIN:custom main:" + packaging + "*/");
                String ending = Pattern.quote("/*END:custom main:" + packaging + "*/");

                mainClassContent = mainClassContent.replaceAll("(?s)" + beginning + ".*?" + ending, "");
            }

            Files.write(mainClass, mainClassContent.getBytes(StandardCharsets.UTF_8));
        } else {
            Files.delete(mainClass);
        }

        // remaining *.java
        if (!includedTechnologies.contains(IncludedTechnology.SERVLET)) {
            Files.delete(javaFiles.resolve("HelloServlet.java"));
            Files.delete(testJavaFiles.resolve("HelloServletIT.java"));
        }
        if (!includedTechnologies.contains(IncludedTechnology.JAX_RS)) {
            Files.delete(javaFiles.resolve("HelloJaxrsApplication.java"));
            Files.delete(javaFiles.resolve("HelloJaxrsResource.java"));
            Files.delete(testJavaFiles.resolve("HelloJaxrsIT.java"));
        }
        if (!includedTechnologies.contains(IncludedTechnology.EJB)) {
            Files.delete(javaFiles.resolve("HelloEjbBean.java"));
            Files.delete(javaFiles.resolve("HelloEjbServlet.java"));
            Files.delete(testJavaFiles.resolve("HelloEjbIT.java"));
        }
    }

    private String dependenciesSnippet(String swarmVersion) {
        StringBuilder result = new StringBuilder();

        if (packaging.hasCustomMain()) {
            result.append("<dependency>\n" +
                          "  <groupId>io.thorntail</groupId>\n" +
                          "  <artifactId>container</artifactId>\n" +
                          "  <version>" + swarmVersion + "</version>\n" +
                          "</dependency>\n");
        }

        if (packaging == Packaging.JAR_WITH_MAIN && dependencies == Dependencies.JAVA_EE_APIS) {
            // for the Main class
            result.append("<dependency>\n" +
                          "  <groupId>io.thorntail</groupId>\n" +
                          "  <artifactId>undertow</artifactId>\n" +
                          "</dependency>\n");
        }

        for (IncludedTechnology technology : includedTechnologies) {
            result.append(technology.dependencySnippet(dependencies));
        }

        result.append(additionalDependency.dependencySnippet());

        return result.toString();
    }

    private String swarmPluginConfigurationSnippet() {
        String result = "<fractionDetectMode>" + autodetection.toSwarmPluginValue() + "</fractionDetectMode>\n";

        if (packaging.hasCustomMain()) {
            result += "<mainClass>org.wildfly.swarm.test.Main</mainClass>\n";
        }

        Optional<String> anotherFraction = additionalFraction.shouldBringFraction();
        if (anotherFraction.isPresent()) {
            result += "<fractions><fraction>" + anotherFraction.get() + "</fraction></fractions>\n";
        }

        return result;
    }

    public boolean hasExplicitFractionDependencies() {
        // with custom main, org.wildfly.swarm:container is always added, and that's an explicit fraction dependency
        return dependencies == Dependencies.FRACTIONS || packaging.hasCustomMain();
    }

    public boolean doesAutodetectionHappen() {
        switch (autodetection) {
            case FORCE:
                return true;
            case NEVER:
                return false;
            case WHEN_MISSING:
                return !hasExplicitFractionDependencies();
            default:
                throw new AssertionError();
        }
    }

    public boolean canRunTests() {
        if (dependencies == Dependencies.JAVA_EE_APIS) {
            return doesAutodetectionHappen();
        }

        return true;
    }

    public Set<String> fractionsThatShouldBePresent() {
        Set<String> expectedFractions = includedTechnologies
                .stream()
                .map(IncludedTechnology::fraction)
                .collect(Collectors.toCollection(HashSet::new));
        if (doesAutodetectionHappen()) {
            additionalDependency.shouldBringFraction().ifPresent(expectedFractions::add);
        }

        if (dependencies == Dependencies.JAVA_EE_APIS && !doesAutodetectionHappen()) {
            expectedFractions = new HashSet<>();
            if (packaging == Packaging.JAR_WITH_MAIN) {
                // always included for the custom main class
                expectedFractions.add(IncludedTechnology.SERVLET.fraction());
            }
        }

        additionalFraction.shouldBringFraction().ifPresent(expectedFractions::add);

        return expectedFractions;
    }

    public Set<String> fractionsThatShouldBeMissing() {
        Set<String> allFractions = Stream.of(IncludedTechnology.values())
                .map(IncludedTechnology::fraction)
                .collect(Collectors.toCollection(HashSet::new));
        allFractions.addAll(AdditionalDependency.allPossibleFractions());
        allFractions.addAll(AdditionalFraction.allPossibleFractions());

        allFractions.removeAll(fractionsThatShouldBePresent());
        return allFractions;
    }

    @Override
    public String toString() {
        return "packaging: " + packaging + ", dependencies: " + dependencies + ", autodetection: " + autodetection
                + ", technologies: " + includedTechnologies + ", additional dependency: " + additionalDependency
                + ", additional fraction: " + additionalFraction;
    }

    // ---

    public String serialize() {
        String technologies = includedTechnologies.stream()
                .map(IncludedTechnology::toString)
                .collect(Collectors.joining(","));
        return packaging + ":" + dependencies + ":" + autodetection + ":" + technologies + ":" + additionalDependency
                + ":" + additionalFraction;
    }

    public static TestingProject deserialize(String string) {
        String[] parts = string.split(":");
        Packaging packaging = Packaging.valueOf(parts[0]);
        Dependencies dependencies = Dependencies.valueOf(parts[1]);
        Autodetection autodetection = Autodetection.valueOf(parts[2]);
        IncludedTechnology[] includedTechnologies = Arrays.stream(parts[3].split(","))
                .map(IncludedTechnology::valueOf)
                .toArray(IncludedTechnology[]::new);
        AdditionalDependency additionalDependency = AdditionalDependency.valueOf(parts[4]);
        AdditionalFraction additionalFraction = AdditionalFraction.valueOf(parts[5]);
        return new TestingProject(packaging, dependencies, autodetection, includedTechnologies, additionalDependency,
                                  additionalFraction);
    }
}
