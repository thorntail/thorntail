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
package org.wildfly.swarm.arquillian.adapter;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;

/**
 * @author Heiko Braun
 * @since 19/10/16
 */
public class GradleDependencyAdapter {

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

    public List<String> parseDependencies() {
        return parseDependencies(Configuration.RUNTIME);
    }

    public List<String> parseDependencies(Configuration configuration) {
        System.out.println(rootPath);

        GradleConnector connector = GradleConnector.newConnector()
                .forProjectDirectory(rootPath.toFile());
        ProjectConnection connection = connector.connect();
        GradleProject project = connection.getModel(GradleProject.class);


        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        connection.newBuild()
                .withArguments("dependencies", "--configuration", configuration.literal)
                .setStandardOutput(bout)
                .run();

        connection.close();


        // parse

        String deps = new String(bout.toByteArray());
        List<String> coordinates = new LinkedList<String>();
        Scanner scanner = new Scanner(deps);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.contains(PREFIX)) {
                line = line.substring(line.indexOf(PREFIX) + PREFIX.length(), line.length());

                if(line.endsWith(SUFFIX)) {
                    line = line.substring(0, line.indexOf(SUFFIX));
                }

                String[] coords = line.split(":");
                if(3==coords.length) {
                    String version = coords[2];
                    if(version.contains(VERSION_UP)) {
                        String s = coords[0]+":"+coords[1]+":"+ version.substring(version.indexOf(VERSION_UP)+VERSION_UP.length(), version.length());
                        System.err.println( "add coordinates: " + s );
                        coordinates.add(s);
                    } else {
                        System.err.println( "add coordinates: " + line );
                        coordinates.add(line);
                    }
                } else {
                    throw new IllegalArgumentException("Unexpected input format");
                }



            }
        }

        scanner.close();

        return coordinates;
    }

    private static final String PREFIX = "--- ";
    private static final String SUFFIX = " (*)";
    private static final String VERSION_UP = "-> ";

    private Path rootPath;
}
