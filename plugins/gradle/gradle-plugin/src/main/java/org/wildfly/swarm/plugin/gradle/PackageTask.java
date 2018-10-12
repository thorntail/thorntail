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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifactSet;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;
import org.wildfly.swarm.fractions.PropertiesUtil;
import org.wildfly.swarm.spi.meta.SimpleLogger;
import org.wildfly.swarm.tools.BuildTool;

/**
 * @author Bob McWhirter
 */
public class PackageTask extends DefaultTask {

    static final String UBERJAR_SUFFIX = "-thorntail";

    static final String HOLLOW_SUFFIX = "-hollow";

    private static final String MODULE_DIR_NAME = "modules";

    private Jar jarTask;

    public Task jarTask(Jar jarTask) {
        this.jarTask = jarTask;

        return this;
    }

    /**
     * Package the application content.
     */
    @TaskAction
    public void packageForSwarm() throws Exception {
        final Project project = getProject();
        final ThorntailExtension extension = getThorntailExtension();

        GradleArtifactResolvingHelper resolvingHelper = new GradleArtifactResolvingHelper(project);
        Properties propertiesFromExtension = extension.getProperties();
        Set<File> moduleDirs = extension.getModules();
        if (moduleDirs.isEmpty()) {
            Path resourcesOutputDir = project.getConvention()
                    .getPlugin(JavaPluginConvention.class)
                    .getSourceSets()
                    .findByName(SourceSet.MAIN_SOURCE_SET_NAME)
                    .getOutput()
                    .getResourcesDir()
                    .toPath()
                    .resolve(MODULE_DIR_NAME);
            if (Files.isDirectory(resourcesOutputDir)) {
                File moduleDir = resourcesOutputDir.toFile();

                moduleDirs.add(moduleDir);
            }
        }


        BuildTool tool = new BuildTool(resolvingHelper)
                .projectArtifact(this.jarTask.getGroup(), this.jarTask.getBaseName(), this.jarTask.getVersion(),
                                 getPackaging(), getProjectArtifactFile())
                .mainClass(getMainClassName())
                .bundleDependencies(extension.isBundleDependencies())
                .executable(extension.isIncludeExecutable())
                .executableScript(extension.getExecutableScript())
                .properties(propertiesFromExtension)
                .properties(getPropertiesFromFile())
                .properties(PropertiesUtil.filteredSystemProperties(propertiesFromExtension, false))
                .fractionDetectionMode(extension.getFractionDetectionMode())
                .hollow(extension.isHollow())
                .additionalModules(moduleDirs.stream()
                                           .filter(File::exists)
                                           .map(File::getAbsolutePath)
                                           .collect(Collectors.toList()))
                .logger(new SimpleLogger() {
                    @Override
                    public void debug(String msg) {
                        getLogger().debug(msg);
                    }

                    @Override
                    public void info(String msg) {
                        getLogger().info(msg);
                    }

                    @Override
                    public void error(String msg) {
                        getLogger().error(msg);
                    }

                    @Override
                    public void error(String msg, Throwable t) {
                        getLogger().error(msg, t);
                    }
                });

        tool.declaredDependencies(GradleToolingHelper.toDeclaredDependencies(extension.getDependencies()));
        tool.bundleDependencies(extension.isBundleDependencies());
        // Add the explicitly defined fractions from the configuration.
        extension.getDeclaredFractions().stream().map(DependencyDescriptor::toArtifactSpec).forEach(tool::fraction);
        // Build the Thorntail archive.
        tool.build(getOutputFile().getName(), getOutputDirectory());

        /* We expect a war task to be present before scanning for war files. */
        final java.util.Optional<Task> task = project.getTasks().stream().filter(t -> "war".equals(t.getName())).findAny();
        if (task.isPresent()) {
            /* Look for all the war archives present */
            final Set<File> warArchives = project.getConfigurations()
                    .stream()
                    .map(Configuration::getAllArtifacts)
                    .map(PublishArtifactSet::getFiles)
                    .map(FileCollection::getFiles)
                    .flatMap(Collection::stream)
                    .filter(f -> f.getName().endsWith(".war"))
                    .collect(Collectors.toSet());
            for (File warArchive : warArchives) {
                tool.repackageWar(warArchive);
            }
        }
    }

    @Input
    private String getPackaging() {
        return jarTask.getExtension();
    }

    @InputFile
    private File getProjectArtifactFile() {
        return jarTask.getArchivePath();
    }

    @Input
    @Optional
    private String getMainClassName() {
        Project project = getProject();
        ThorntailExtension thorntailExtension = getThorntailExtension();
        String mainClassName = thorntailExtension.getMainClassName();

        if (mainClassName == null && project.getConvention().getPlugins().containsKey("application")) {
            ApplicationPluginConvention app = (ApplicationPluginConvention) project.getConvention().getPlugins().get("application");
            mainClassName = app.getMainClassName();
        }

        if (mainClassName != null && !mainClassName.equals("")) {
            getLogger().warn(
                    "\n------\n" +
                            "Custom main() usage is intended to be deprecated in a future release and is no longer supported, \n" +
                            "please refer to http://docs.thorntail.io for YAML configuration that replaces it." +
                            "\n------"
            );
        }

        return mainClassName;
    }

    private ThorntailExtension getThorntailExtension() {
        return getProject().getExtensions().getByType(ThorntailExtension.class);
    }

    @Input
    private Properties getPropertiesFromFile() {
        final Properties properties = new Properties();
        File propertiesFile = getPropertiesFile();

        if (propertiesFile != null) {
            try {
                properties.putAll(PropertiesUtil.loadProperties(propertiesFile));
            } catch (IOException e) {
                getLogger().error("Failed to load properties from " + propertiesFile, e);
            }
        }
        return properties;
    }

    @Optional
    @InputFile
    private File getPropertiesFile() {
        return getThorntailExtension().getPropertiesFile();
    }

    @OutputFile
    private File getOutputFile() {
        return BuildTool.getOutputFile(getBaseName() + UBERJAR_SUFFIX + ".jar", getOutputDirectory());
    }

    private String getBaseName() {
        return getProject().getName() + (getThorntailExtension().isHollow() ? HOLLOW_SUFFIX : "");
    }

    private Path getOutputDirectory() {
        return getProject().getBuildDir().toPath().resolve("libs");
    }
}
