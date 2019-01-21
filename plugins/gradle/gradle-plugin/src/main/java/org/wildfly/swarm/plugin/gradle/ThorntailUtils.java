package org.wildfly.swarm.plugin.gradle;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.bundling.Jar;
import org.wildfly.swarm.fractions.PropertiesUtil;

public final class ThorntailUtils {

    private static final Logger LOGGER = Logging.getLogger(ThorntailUtils.class);

    private ThorntailUtils() {
    }

    public static Properties getPropertiesFromFile(File file) {
        final Properties properties = new Properties();

        if (file != null) {
            try {
                properties.putAll(PropertiesUtil.loadProperties(file));
            } catch (IOException e) {
                LOGGER.error("Failed to load properties from " + file, e);
            }
        }
        return properties;
    }

    /**
     * Returns the most suitable Archive-Task for wrapping in the swarm jar - in the following order:
     * <p>
     * 1. Custom-JAR-Task defined in ThorntailExtension 'archiveTask'
     * 2. WAR-Task
     * 3. JAR-Task
     */
    public static Jar getArchiveTask(Project project) {

        TaskCollection<Jar> existingArchiveTasks = project.getTasks().withType(Jar.class);
        Jar customArchiveTask = project.getExtensions().getByType(ThorntailExtension.class).getArchiveTask();

        if (customArchiveTask != null) {
            return existingArchiveTasks.getByName(customArchiveTask.getName());

        } else if (existingArchiveTasks.findByName(WarPlugin.WAR_TASK_NAME) != null) {
            return existingArchiveTasks.getByName(WarPlugin.WAR_TASK_NAME);

        } else if (existingArchiveTasks.findByName(JavaPlugin.JAR_TASK_NAME) != null) {
            return existingArchiveTasks.getByName(JavaPlugin.JAR_TASK_NAME);
        }

        throw new GradleException("Unable to detect Archive-Task: project contains neither 'war' nor 'jar', " +
                "nor is custom Archive-Task specified in the \"thorntail\" extension.");
    }


}
