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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import org.gradle.api.Project;

/**
 * Default implementation of the {@link ThorntailApplicationModel} interface. This type is built & populated by the Gradle plugin.
 */
public class DefaultThorntailApplicationModel implements ThorntailApplicationModel {

    private final String pluginVersion;

    private Map<File, String> pathToProjectMapping = new HashMap<>();

    private Map<String, ThorntailConfiguration> projectToConfigMapping = new HashMap<>();

    /**
     * Constructs a new instance of {@code DefaultThorntailApplicationModel}.
     */
    public DefaultThorntailApplicationModel() {
        this.pluginVersion = GradleDependencyResolutionHelper.determinePluginVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThorntailConfiguration getConfiguration(File projectFolder) {
        return getConfiguration(pathToProjectMapping.get(projectFolder));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThorntailConfiguration getConfiguration(String projectName) {
        return projectToConfigMapping.get(projectName);
    }

    /**
     * Analyze the given project and store it's properties.
     *
     * @param project the Gradle project reference.
     */
    public void analyzeProject(Project project) {
        Preconditions.checkNotNull(project, "Gradle project reference cannot be null.");
        pathToProjectMapping.put(project.getProjectDir(), project.getName());
        ThorntailExtension extension = project.getExtensions().findByType(ThorntailExtension.class);
        if (extension != null) {
            // This method is invoked only for building the model that can be exported across the wire to the Arquillian
            // adapter. So, we will resolve the dependencies as part of this method.
            extension.getDependencies();
        }
        projectToConfigMapping.put(project.getName(), extension);
    }
}
