/*
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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaProject;
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

    public GradleDependencyAdapter(Path projectDir) {
        this.rootPath = projectDir;
    }

    public DeclaredDependencies getProjectDependencies() {
        GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(rootPath.toFile());
        ProjectConnection connection = connector.connect();
        try {
            return getProjectDependencies(connection);
        } finally {
            connection.close();
        }
    }

    /**
     * Retrieve the project dependencies by analyzing the IdeaProject model.
     *
     * @param connection the Gradle project connection.
     * @return the project dependencies.
     */
    private DeclaredDependencies getProjectDependencies(ProjectConnection connection) {
        // Of the 4 available scopes for a IdeaDependency scope, we ignore "PROVIDED" & "RUNTIME".
        final List<String> APPLICABLE_SCOPES = Arrays.asList("COMPILE", "TEST");
        DeclaredDependencies dependencies = new DeclaredDependencies();

        // 1. Get the IdeaProject model from the Gradle connection.
        IdeaProject prj = connection.getModel(IdeaProject.class);

        // 2. Find the IdeaModule that maps to the project that we are looking at.
        Optional<? extends IdeaModule> result = prj.getModules().stream()
                .filter(m -> m.getGradleProject().getProjectDirectory().toPath().equals(rootPath))
                .findFirst();

        // 3. Parse the dependencies and add them to the return object.
        result.ifPresent(m -> m.getDependencies().stream()
                .filter(d -> APPLICABLE_SCOPES.contains(d.getScope().getScope().toUpperCase()) && d instanceof ExternalDependency)
                .forEach(d -> {
                    // Construct the ArtifactSpec and add to the declared dependencies.
                    ExternalDependency extDep = (ExternalDependency) d;
                    GradleModuleVersion gav = extDep.getGradleModuleVersion();
                    if (gav != null) {
                        ArtifactSpec spec = new ArtifactSpec("compile", gav.getGroup(), gav.getName(), gav.getVersion(),
                                                             "jar", null, extDep.getFile());
                        dependencies.add(spec);
                    } else {
                        System.err.println("Skipping artifact since it doesn't have any GAV coordinates: " + d.toString());
                    }
                }));

        return dependencies;
    }

    private Path rootPath;
}
