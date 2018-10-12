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

import org.gradle.tooling.model.Dependency;
import org.gradle.tooling.model.GradleModuleVersion;
import org.wildfly.swarm.tools.ArtifactSpec;

/**
 * The {@code DependencyDescriptor} type represents a dependency's coordinates that can serialized over the wire. This is
 * required for scenarios where integrations happen via Gradle's tooling API (e.g., Arquillian adapters). It would be nice
 * if the actual ArtifactSpec type had a parent interface so that we can avoid having to use this scheme of doing things.
 */
public interface DependencyDescriptor extends GradleModuleVersion, Dependency, Serializable {

    /**
     * Get the scope (compile, runtime, test, etc.) for this artifact descriptor.
     */
    default String getScope() {
        return "compile";
    }

    /**
     * Get the packaging type associated with this dependency.
     */
    String getType();

    /**
     * Get the classifier associated with this dependency.
     */
    String getClassifier();

    /**
     * Get the location of the resolved artifact (if available).
     */
    File getFile();

    /**
     * Translate this descriptor in to a reference of {@link ArtifactSpec}.
     */
    default ArtifactSpec toArtifactSpec() {
        return new ArtifactSpec(getScope(), getGroup(), getName(), getVersion(), getType(), getClassifier(), getFile());
    }

}
