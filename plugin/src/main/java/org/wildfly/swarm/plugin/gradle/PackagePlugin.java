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
