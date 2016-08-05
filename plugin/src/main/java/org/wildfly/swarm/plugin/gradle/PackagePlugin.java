/**
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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;

/**
 * @author Bob McWhirter
 */
public class PackagePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("swarm", SwarmExtension.class, project);
        project.afterEvaluate(__ -> {
            final TaskContainer tasks = project.getTasks();
            final PackageTask packageTask = tasks.create("wildfly-swarm-package", PackageTask.class);
            tasks.withType(Jar.class, task -> packageTask.jarTask(task).dependsOn(task));
            tasks.getByName("build").dependsOn(packageTask);
        });
    }
}
