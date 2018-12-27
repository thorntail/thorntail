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

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

/**
 * @author Bob McWhirter
 */
@SuppressWarnings("UnstableApiUsage")
public class PackagePlugin extends AbstractThorntailPlugin {

    /**
     * The name of the task that is responsible for repackaging the project archive.
     */
    public static final String THORNTAIL_PACKAGE_TASK_NAME = "thorntail-package";

    /**
     * Constructs a new instance of {@code PackagePlugin}, which is initialized with the Gradle tooling model builder registry.
     *
     * @param registry the Gradle project's {@code ToolingModelBuilderRegistry}.
     */
    @Inject
    public PackagePlugin(ToolingModelBuilderRegistry registry) {
        super(registry);
    }

    @Override
    public void apply(Project project) {
        super.apply(project);
        PluginManager pluginManager = project.getPluginManager();
        if (pluginManager.hasPlugin(ThorntailArquillianPlugin.PLUGIN_ID)) {
            pluginManager.apply(ThorntailArquillianPlugin.class);
        }
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

}
