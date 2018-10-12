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
import java.io.Serializable;

import org.gradle.tooling.model.Model;

/**
 * The {@code ThorntailApplicationModel} type acts as the bridge between a Gradle project and its Thorntail application definition.
 * This type is exposed via the Gradle's tooling API and is expected to be leveraged by both Gradle and non-Gradle based
 * environments, e.g., Arquillian adapters. In a multi-module Gradle project, there could be multiple sub-projects that have
 * their own independent Thorntail application configurations. For such scenarios, consumers need to query for the
 * {@link ThorntailConfiguration} appropriate for the project. Consumers can query either by the project's folder path or the
 * project's name.
 */
public interface ThorntailApplicationModel extends Model, Serializable {

    /**
     * Get the version of the Thorntail plugin used for building this application model. This is useful when attempting to resolve
     * dependencies or performing additional version checks.
     *
     * @return the version of the Thorntail plugin used for building this application model.
     */
    String getPluginVersion();

    /**
     * Get the {@link ThorntailConfiguration} associated with Gradle project present in the given {@code project folder}.
     *
     * @param projectFolder the path to the Gradle project's base directory.
     * @return the associated {@link ThorntailConfiguration} if available, or null.
     */
    ThorntailConfiguration getConfiguration(File projectFolder);

    /**
     * Get the {@link ThorntailConfiguration} associated with Gradle project identified by the given {@code project name}.
     *
     * @param projectName the name of the Gradle project.
     * @return the associated {@link ThorntailConfiguration} if available, or null.
     */
    ThorntailConfiguration getConfiguration(String projectName);
}
