/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;

/**
 * @author Bob McWhirter
 */
public class PackagePlugin implements Plugin<Project> {

    @Override
    public void apply(Project p) {
        p.getExtensions().create( "swarm", SwarmExtension.class );
        p.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                PackageTask t = p.getTasks().create("wildfly-swarm-package", PackageTask.class);
                p.getTasks().withType(Jar.class, (task) -> {
                    task.getArchivePath();
                    t.dependsOn(task);
                    t.jarTask( task );
                });
                Task buildTask = p.getTasks().getByName("build");
                buildTask.dependsOn( t );
            }
        });
    }
}
