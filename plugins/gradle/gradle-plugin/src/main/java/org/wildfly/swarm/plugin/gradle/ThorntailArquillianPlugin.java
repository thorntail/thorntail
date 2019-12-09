/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wildfly.swarm.plugin.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.wildfly.swarm.plugin.gradle.GradleDependencyResolutionHelper.determinePluginVersion;

/**
 * Gradle plugin for enabling Arquillian tests based on Thorntail. This is useful in cases where a Gradle project doesn't
 * require the Package task, e.g., library projects.
 */
public class ThorntailArquillianPlugin extends AbstractThorntailPlugin {

    public static final String PLUGIN_ID = "thorntail-arquillian";

    private static final String BUILD_SCRIPT_FRAGMENT = "META-INF/thorntail-scripts/configure-tasks.groovy";

    private boolean projectConfigured = false;

    /**
     * Constructs a new instance of {@code PackagePlugin}, which is initialized with the Gradle tooling model builder registry.
     *
     * @param registry the Gradle project's {@code ToolingModelBuilderRegistry}.
     */
    @SuppressWarnings("UnstableApiUsage")
    @Inject
    public ThorntailArquillianPlugin(ToolingModelBuilderRegistry registry) {
        super(registry);
    }

    @Override
    public void apply(Project project) {
        if (projectConfigured) {
            // Nothing to do.
            return;
        }
        projectConfigured = true;

        super.apply(project);

        //noinspection Convert2Lambda
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project __) {

                // Add a dependency on the Arquillian library
                addDependency("testImplementation", "io.thorntail:arquillian:" + determinePluginVersion());

                // Add the Gradle tooling dependencies to the test runtime.
                addDependency("testRuntimeOnly", "org.gradle:gradle-tooling-api:" + project.getGradle().getGradleVersion());
                addDependency("testRuntimeOnly", "io.thorntail:gradle-arquillian-adapter:" + determinePluginVersion());

                URL resource = ThorntailArquillianPlugin.class.getClassLoader().getResource(BUILD_SCRIPT_FRAGMENT);
                if (resource != null) {
                    try {
                        if (!project.getBuildDir().exists()) {
                            project.getBuildDir().mkdirs();
                        }
                        File scriptFile = new File(project.getBuildDir(), "thorntail-arquillian-script.gradle");
                        if (!scriptFile.exists()) {
                            scriptFile.createNewFile();
                            String[] lines = readString(resource.openStream()).split("\n");
                            Files.write(scriptFile.toPath(), Arrays.asList(lines));
                        }
                        Map<String, Object> map = Collections.singletonMap("from", scriptFile.getAbsolutePath());
                        project.apply(map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Thorntail Arquillian:: Unable to locate resource: " + BUILD_SCRIPT_FRAGMENT);
                }
            }
        });
    }

    /**
     * Read the given stream in to a String. This method will close the stream as well.
     *
     * @param stream the input stream.
     * @return the string built out of the data available in the given stream.
     */
    private static String readString(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8.name());
        } finally {
            stream.close();
        }
    }

}
