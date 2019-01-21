package org.wildfly.swarm.plugin.gradle;

import java.util.concurrent.TimeUnit;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.wildfly.swarm.tools.exec.SwarmProcess;

public class StopTask extends DefaultTask {

    @TaskAction
    public void stopThorntailProcess() {
        ThorntailExtension extension = getProject().getExtensions().getByType(ThorntailExtension.class);

        SwarmProcess process = (SwarmProcess) getProject().findProperty(PackagePlugin.THORNTAIL_PROCESS_PROPERTY);
        if (process == null) {
            getLogger().error("No Thorntail process was found to stop.");
            return;
        }
        try {
            getLogger().info("Stopping Thorntail process.");
            process.stop(extension.getStopTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            // Do nothing
        } finally {
            process.destroyForcibly();
        }
    }
}
