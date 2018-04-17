/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.arquillian.adapter.gradle;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringJoiner;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.DeclaredDependencies;

/**
 * Get the dependency details for a Gradle project. This adapter makes use of the IdeaProject definition that is provided by
 * default with every Gradle installation in order to determine the target set of dependencies.
 *
 * @author Heiko Braun
 * @since 19/10/16
 */
public class GradleDependencyAdapter {

    private static final String PREFIX1 = "+---";

    private static final String PREFIX2 = "\\---";

    private static final String PROJECT = "project";

    private static final String COLON = ":";

    /**
     * A gradle build configuration reference
     */
    public enum Configuration {
        ARCHIVE("archives"),
        DEFAULT("default"),
        COMPILE("compile"),
        RUNTIME("runtime"),
        TEST_COMPILE("testCompile"),
        TEST_RUNTIME("testRuntime");

        private String literal;

        Configuration(String literal) {
            this.literal = literal;
        }
    }

    public GradleDependencyAdapter(Path projectDir) {
        this.rootPath = projectDir;
    }

    public DeclaredDependencies parseDependencies(Configuration configuration) {
        GradleConnector connector = GradleConnector.newConnector()
                .forProjectDirectory(rootPath.toFile());
        ProjectConnection connection = connector.connect();
        GradleProject project = connection.getModel(GradleProject.class);
        String path = project.getPath(); // Get the Gradle project path for executing the dependencies section.
        project = getProjectForDirectory(project, rootPath);
        if (project != null) {
            // Not sure if we would ever have a scenario where project is null. Even if it does happen, it would fall back to
            // existing default behavior.
            path = project.getPath();
            if (!path.endsWith(":")) {
                path = path + ":";
            }
        }

        // Identify and resolve the dependencies for the given project.
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        connection.newBuild()
                .withArguments(path + "dependencies", "--configuration", configuration.literal)
                .setStandardOutput(bout)
                .run();

        connection.close();


        // parse
        DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        String deps = new String(bout.toByteArray());

        Scanner scanner = new Scanner(deps);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // top level deps
            if (line.startsWith(PREFIX1)
                    || line.startsWith(PREFIX2)) {

                if (stack.size() > 0) {
                    stack.pop();
                }

                // parse
                line = parseLine(line);
                String coord = parseCoordinate(line);
                ArtifactSpec parent = DeclaredDependencies.createSpec(coord);
                declaredDependencies.add(parent);
                stack.push(parent);
            } else if (line.contains(PREFIX)) {
                // transient
                line = parseLine(line);

                if (line.startsWith(PROJECT)) { // Always skip 'project' dependencies.
                    continue;
                }

                String coord = parseCoordinate(line);
                declaredDependencies.add(stack.peek(), DeclaredDependencies.createSpec(coord));

            }
        }

        scanner.close();

        return declaredDependencies;
    }

    private String parseCoordinate(String line) {
        String[] coords = line.split(COLON);
        if (3 == coords.length) {
            String version = coords[2];
            if (version.contains(VERSION_UP)) {
                String s = coords[0] + ":" + coords[1] + ":" + version.substring(version.indexOf(VERSION_UP) + VERSION_UP.length(), version.length());
                return s;
            } else {
                return line;
            }
        } else if (2 == coords.length) {
            // This could happen if the Gradle project is making use of the Gradle feature (in preview mode for 4.6+) that has
            // improved support for BOMs.
            // In this particular scenario, the dependency should look like the following,
            // org.wildfly.swarm:jaxrs-multipart -> 2018.5.0-plankton-SNAPSHOT

            StringJoiner joiner = new StringJoiner(":");
            joiner.add(coords[0]);

            // Extract artifact & version details.
            String artifactId = coords[1];
            int idx = artifactId.indexOf(VERSION_UP);
            if (idx != -1) {
                String version = artifactId.substring(idx + VERSION_UP.length()).trim();
                artifactId = artifactId.substring(0, idx).trim();
                joiner.add(artifactId).add(version);
                return joiner.toString();
            }
        }
        // Raise an exception if we are unable to determine the dependency format.
        throw new IllegalArgumentException("Unexpected dependency format: " + line);
    }

    private String parseLine(String line) {
        line = line.substring(line.indexOf(PREFIX) + PREFIX.length(), line.length());

        if (line.endsWith(SUFFIX)) {
            line = line.substring(0, line.indexOf(SUFFIX));
        }
        return line;
    }

    /**
     * Scan the project hierarchy (in case of a multi-module project) and return the project that matches the given path
     * reference.
     *
     * @param project the Gradle project reference.
     * @param path    the path to match against.
     * @return the Gradle project reference whose path matches the given input path.
     */
    private static GradleProject getProjectForDirectory(GradleProject project, Path path) {
        if (project != null) {
            if (project.getProjectDirectory().toPath().equals(path)) {
                return project;
            }
            for (GradleProject p : project.getChildren()) {
                GradleProject searchResult = getProjectForDirectory(p, path);
                if (searchResult != null) {
                    return searchResult;
                }
            }
        }
        return null;
    }

    private static final String PREFIX = "--- ";

    private static final String SUFFIX = " (*)";

    private static final String VERSION_UP = "-> ";

    private Stack<ArtifactSpec> stack = new Stack<>();

    private Path rootPath;
}
