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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import groovy.lang.Closure;
import groovy.util.ConfigObject;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;
import org.wildfly.swarm.tools.BuildTool;

/**
 * The opinionated configuration for the Thorntail plugin.
 *
 * @author Bob McWhirter
 */
public class ThorntailExtension implements ThorntailConfiguration {

    private String pluginVersion;

    private String mainClass;

    private Boolean bundleDependencies = true;

    private Boolean executable = false;

    private File executableScript;

    private Properties properties = new Properties();

    private File propertiesFile;

    private Set<String> fractions = new HashSet<>();

    private Set<File> moduleDirs = new HashSet<>();

    // Skip serialization of the archive task.
    private transient Jar archiveTask;

    private BuildTool.FractionDetectionMode fractionDetectMode = BuildTool.FractionDetectionMode.when_missing;

    private boolean hollow = false;

    private Map<DependencyDescriptor, Set<DependencyDescriptor>> dependencyMap;

    private Map<DependencyDescriptor, Set<DependencyDescriptor>> testDependencyMap;

    // Transient references that will not be included as part of Gradle's cache serialization.
    private transient Project project;

    /**
     * Default constructor for (de)serializing the configuration across processes.
     */
    public ThorntailExtension(Project project) {
        this.project = project;
        this.pluginVersion = GradleDependencyResolutionHelper.determinePluginVersion();
    }

    /**
     * Closure for applying properties to the Swarm configuration.
     */
    public void properties(Closure<Properties> closure) {
        ConfigObject config = new ConfigObject();
        closure.setResolveStrategy(Closure.DELEGATE_ONLY);
        closure.setDelegate(config);
        closure.call();
        config.flatten(this.properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMainClassName() {
        return this.mainClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMainClassName(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBundleDependencies() {
        return bundleDependencies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBundleDependencies(boolean bundleDependencies) {
        this.bundleDependencies = bundleDependencies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIncludeExecutable() {
        return executable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIncludeExecutable(boolean executable) {
        this.executable = executable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getExecutableScript() {
        return executableScript;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutableScript(File executableScript) {
        this.executableScript = executableScript;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getPropertiesFile() {
        return propertiesFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPropertiesFile(final File propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getFractions() {
        return fractions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFractions(Set<String> fractions) {
        this.fractions.clear();
        if (fractions != null) {
            this.fractions.addAll(fractions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<File> getModules() {
        return moduleDirs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModules(final Set<File> moduleDirs) {
        this.moduleDirs.clear();
        if (moduleDirs != null) {
            this.moduleDirs.addAll(moduleDirs);
        }
    }

    public Jar getArchiveTask() {
        return archiveTask;
    }

    public void setArchiveTask(Jar archiveTask) {
        this.archiveTask = archiveTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildTool.FractionDetectionMode getFractionDetectionMode() {
        return fractionDetectMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFractionDetectionMode(BuildTool.FractionDetectionMode fractionDetectMode) {
        this.fractionDetectMode = fractionDetectMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHollow(boolean hollow) {
        this.hollow = hollow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHollow() {
        return hollow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<DependencyDescriptor, Set<DependencyDescriptor>> getDependencies() {
        if (dependencyMap == null) {
            dependencyMap = GradleDependencyResolutionHelper.determineProjectDependencies(project, "runtimeClasspath", false);
        }
        return dependencyMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<DependencyDescriptor, Set<DependencyDescriptor>> getTestDependencies() {
        if (testDependencyMap == null) {
            testDependencyMap = GradleDependencyResolutionHelper.determineProjectDependencies(project, "testRuntimeClasspath", true);

            // The Arquillian adapter uses its own ArtifactResolver which makes things a little challenging.
            // We need to prune the map for "project" dependencies and make sure they have no dependencies explicitly called out.
            // This will ensure that there is no transitive resolution being performed on it as "isPresolved" will return true.
            Set<DependencyDescriptor> addFinally = new HashSet<>();
            Map<DependencyDescriptor, Set<DependencyDescriptor>> depPrj = new HashMap<>();
            testDependencyMap.forEach((key, values) -> {

                // Check if any of the elements in the "value" set represent a dependent project-module.
                // If they do, then move it to the top level.
                Iterator<DependencyDescriptor> itr = values.iterator();
                while (itr.hasNext()) {
                    DependencyDescriptor d = itr.next();
                    if (GradleDependencyResolutionHelper.isProject(project, d)) {
                        itr.remove();
                        addFinally.add(d);
                    }
                }

                // Check if the "key" itself represents a dependent project-module.
                // If it does, then move all the children to top-level.
                if (GradleDependencyResolutionHelper.isProject(project, key)) {
                    addFinally.addAll(values);
                    values.clear();
                }
            });

            addFinally.forEach(d -> testDependencyMap.putIfAbsent(d, Collections.emptySet()));
        }
        return testDependencyMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginVersion() {
        return pluginVersion;
    }

    ///
    /// Equals & hashcode -- This allows us to simplify the task input settings.
    ///

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ThorntailExtension)) {
            return false;
        }
        ThorntailExtension extension = (ThorntailExtension) o;
        return bundleDependencies == extension.bundleDependencies &&
                executable == extension.executable &&
                hollow == extension.hollow &&
                Objects.equals(pluginVersion, extension.pluginVersion) &&
                Objects.equals(mainClass, extension.mainClass) &&
                Objects.equals(executableScript, extension.executableScript) &&
                Objects.equals(properties, extension.properties) &&
                Objects.equals(propertiesFile, extension.propertiesFile) &&
                Objects.equals(fractions, extension.fractions) &&
                Objects.equals(moduleDirs, extension.moduleDirs) &&
                fractionDetectMode == extension.fractionDetectMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginVersion, mainClass, bundleDependencies, executable, executableScript, properties,
                            propertiesFile, fractions, moduleDirs, fractionDetectMode, hollow);
    }
}
