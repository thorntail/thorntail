/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.plugin.gradle;

import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

/**
 * @author Bob McWhirter
 */
public class PackagePlugin implements Plugin<Project> {

    /**
     * The name of the Gradle configuration.
     */
    public static final String THORNTAIL_EXTENSION = "thorntail";

    /**
     * The name of the task that is responsible for repackaging the project archive.
     */
    public static final String THORNTAIL_PACKAGE_TASK_NAME = "thorntail-package";

    private final ToolingModelBuilderRegistry registry;

    /**
     * Constructs a new instance of {@code PackagePlugin}, which is initialized with the Gradle tooling model builder registry.
     *
     * @param registry the Gradle project's {@code ToolingModelBuilderRegistry}.
     */
    @Inject
    public PackagePlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(Project project) {
        registry.register(new ThorntailToolingModelBuilder());
        ThorntailExtension extension = new ThorntailExtension(project);
        project.getExtensions().add(THORNTAIL_EXTENSION, extension);
        project.afterEvaluate(__ -> {
            final TaskContainer tasks = project.getTasks();
            final PackageTask packageTask = tasks.create(THORNTAIL_PACKAGE_TASK_NAME, PackageTask.class);

            final Jar archiveTask = getArchiveTask(project);

            if (archiveTask == null) {
                throw new GradleException("No suitable Archive-Task found to include in Swarm Uber-JAR.");
            }

            packageTask.jarTask(archiveTask).dependsOn(archiveTask);

            tasks.getByName(JavaBasePlugin.BUILD_TASK_NAME).dependsOn(packageTask);
        });
    }

    /**
     * Returns the most suitable Archive-Task for wrapping in the swarm jar - in the following order:
     *
     * 1. Custom-JAR-Task defined in ThorntailExtension 'archiveTask'
     * 2. WAR-Task
     * 3. JAR-Task
     */
    private Jar getArchiveTask(Project project) {

        TaskCollection<Jar> existingArchiveTasks = project.getTasks().withType(Jar.class);
        Jar customArchiveTask = project.getExtensions().getByType(ThorntailExtension.class).getArchiveTask();

        if (customArchiveTask != null) {
            return existingArchiveTasks.getByName(customArchiveTask.getName());

        } else if (existingArchiveTasks.findByName(WarPlugin.WAR_TASK_NAME) != null) {
            return existingArchiveTasks.getByName(WarPlugin.WAR_TASK_NAME);

        } else if (existingArchiveTasks.findByName(JavaPlugin.JAR_TASK_NAME) != null) {
            return existingArchiveTasks.getByName(JavaPlugin.JAR_TASK_NAME);
        }

        return null;
    }

    /**
     * The extension of Gradle's ToolingModelBuilder. This class is responsible for building the {@link ThorntailApplicationModel}
     * model that will be exported to external tools, e.g., the Arquillian adapter for Gradle projects.
     */
    private static class ThorntailToolingModelBuilder implements ToolingModelBuilder {

        /**
         * Return true if the requested {@code model name} is of {@link ThorntailApplicationModel} type.
         *
         * @param modelName the name of the requested model.
         * @return true if the requested {@code model name} is of {@link ThorntailApplicationModel} type.
         */
        @Override
        public boolean canBuild(String modelName) {
            return ThorntailApplicationModel.class.getName().equals(modelName);
        }

        /**
         * Build and return the {@link ThorntailApplicationModel} model for the requested project.
         *
         * @param modelName the name of the requested model.
         * @param project   the Gradle project reference.
         * @return the fully built SwarmApplicationModel model.
         */
        @SuppressWarnings("unchecked")
        @Override
        public Object buildAll(String modelName, Project project) {
            if (!canBuild(modelName)) {
                throw new IllegalArgumentException("Unsupported model requested: " + modelName);
            }
            Project rootProject = project.getRootProject();
            Map<String, Object> p = (Map<String, Object>) rootProject.getProperties();
            return p.computeIfAbsent(ThorntailApplicationModel.class.getName(), n -> {
                DefaultThorntailApplicationModel app = new DefaultThorntailApplicationModel();
                rootProject.getAllprojects().forEach(app::analyzeProject);
                return app;
            });
        }
    }
}
