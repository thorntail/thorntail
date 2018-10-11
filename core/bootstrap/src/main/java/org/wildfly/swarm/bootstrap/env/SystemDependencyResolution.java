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
package org.wildfly.swarm.bootstrap.env;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author Heiko Braun
 * @since 18/07/16
 */
class SystemDependencyResolution implements DependencyResolution {


    SystemDependencyResolution() {
        final String classpathProp = System.getProperty("java.class.path");
        final String javaHomProp = System.getProperty("java.home");
        final String userDirProp = System.getProperty("user.dir");
        final String testClasspatProp = System.getProperty("thorntail.test.dependencies");

        //Dedect gradle cache
        this.useGradleRepo = classpathProp.contains(File.separator + ".gradle");

        this.classpath = Arrays.asList(classpathProp.split(File.pathSeparator));
        this.testClasspath = testClasspatProp != null ? Arrays.asList(testClasspatProp.split(File.pathSeparator)) : Collections.emptyList();

        this.pwd = userDirProp;
        this.javaHome = javaHomProp.endsWith(JRE) ? javaHomProp.substring(0, javaHomProp.lastIndexOf(JRE)) : javaHomProp;
    }

    @Override
    public Set<String> resolve(List<String> exclusions) throws IOException {

        final Set<String> archivesPaths = new HashSet<>();

        exclusions.replaceAll(s -> s.replace('.', File.separatorChar));

        if (classpath != null) {
            ApplicationEnvironment env = ApplicationEnvironment.get();
            Set<String> classpathElements = new HashSet<>();
            Set<String> providedGAVs = new HashSet<>();
            List<String> testClasspathElements = testClasspath != null ? testClasspath : Collections.emptyList();

            for (final String element : classpath) {
                if (!element.startsWith(javaHome) && !element.startsWith(pwd + File.separatorChar) && !element.endsWith(".pom")) {
                    // explicit exclusions
                    if (!excluded(exclusions, element)) {
                        classpathElements.add(element);
                    }
                }
            }

            //  prepare the list of provided dep's, these will be implicitly excluded
            providedGAVs.addAll(
                    env.getRemovableDependencies()
                            .stream()
                            .map(e -> e.split(":"))
                            .map(e -> e[0] + File.separatorChar + e[1] + File.separatorChar)
                            .map(m -> (useGradleRepo ? m : m.replace('.', File.separatorChar)))
                            .collect(Collectors.toList())
            );


            // implicit exclusions
            for (final String element : classpathElements) {
                boolean excludedByProvidedGAVs = excluded(providedGAVs, element);
                boolean excludedByTestClasspath = excluded(testClasspathElements, element);

                if (!excludedByProvidedGAVs && !excludedByTestClasspath) {
                    archivesPaths.add(element);
                }
            }
        }

        return archivesPaths;
    }

    private static final String JRE = "jre";

    private final List<String> classpath;

    private final String javaHome;

    private final String pwd;

    private final List<String> testClasspath;

    private final boolean useGradleRepo;

}
