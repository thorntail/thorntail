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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import groovy.lang.Closure;
import groovy.util.ConfigObject;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.Project;
import org.wildfly.swarm.tools.BuildTool;

/**
 * @author Bob McWhirter
 */
public class SwarmExtension {

    private Project project;

    private String mainClass;

    private Boolean bundleDependencies = true;

    private Boolean executable = false;

    private File executableScript;

    private Properties properties = new Properties();

    private File propertiesFile;

    private List<File> moduleDirs = new ArrayList<>();

    private Jar archiveTask;

    private BuildTool.FractionDetectionMode fractionDetectMode = BuildTool.FractionDetectionMode.when_missing;

    private Boolean hollow = false;

    public SwarmExtension(Project project) {
        this.project = project;
    }

    public void properties(Closure<Properties> closure) {
        ConfigObject config = new ConfigObject();
        closure.setResolveStrategy(Closure.DELEGATE_ONLY);
        closure.setDelegate(config);
        closure.call();
        config.flatten(this.properties);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public String getMainClassName() {
        return this.mainClass;
    }

    public void setMainClassName(String mainClass) {
        this.mainClass = mainClass;
    }

    public Boolean getBundleDependencies() {
        return bundleDependencies;
    }

    public void setBundleDependencies(Boolean bundleDependencies) {
        this.bundleDependencies = bundleDependencies;
    }

    public Boolean getExecutable() {
        return executable;
    }

    public void setExecutable(Boolean executable) {
        this.executable = executable;
    }

    public File getExecutableScript() {
        return executableScript;
    }

    public void setExecutableScript(File executableScript) {
        this.executableScript = executableScript;
    }

    public File getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(final File propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public List<File> getModuleDirs() {
        return moduleDirs;
    }

    public void setModuleDirs(final List<File> moduleDirs) {
        this.moduleDirs.clear();
        this.moduleDirs.addAll(moduleDirs);
    }

    public Jar getArchiveTask() {
        return archiveTask;
    }

    public void setArchiveTask(Jar archiveTask) {
        this.archiveTask = archiveTask;
    }

    public BuildTool.FractionDetectionMode getFractionDetectMode() {
        return fractionDetectMode;
    }

    public void setFractionDetectMode(BuildTool.FractionDetectionMode fractionDetectMode) {
        this.fractionDetectMode = fractionDetectMode;
    }

    public void setHollow(Boolean hollow) {
        this.hollow = hollow;
    }

    public Boolean getHollow() {
        return hollow;
    }
}
