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

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

/**
 * Gradle plugin for enabling Arquillian tests based on Thorntail. This is useful in cases where a Gradle project doesn't
 * require the Package task, e.g., library projects.
 */
public class ThorntailArquillianPlugin extends AbstractThorntailPlugin {

    public static final String PLUGIN_ID = "thorntail-arquillian";

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
        super.apply(project);
        // Add the Gradle tooling dependency if it is missing.
        Configuration testRuntimeConfig = project.getConfigurations().findByName("testRuntimeClasspath");
        if (testRuntimeConfig != null) {
            project.getDependencies().add("testRuntimeClasspath",
                                          "org.gradle:gradle-tooling-api:" + project.getGradle().getGradleVersion());
        }
    }
}
