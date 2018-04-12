/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.plugin.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomUtils;
import org.wildfly.swarm.tools.exec.SwarmExecutor;
import org.wildfly.swarm.tools.exec.SwarmProcess;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(name = "multistart",
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MultiStartMojo extends AbstractSwarmMojo {

    private static final String SWARM_PROCESS = "swarm-process";

    @Parameter(alias = "processes")
    protected List<XmlPlexusConfiguration> processes;

    @Component
    protected BuildPluginManager pluginManager;

    @Override
    public void executeSpecific() throws MojoExecutionException, MojoFailureException {

        initProperties(true);
        initEnvironment();

        for (XmlPlexusConfiguration process : this.processes) {
            try {
                start(process);
            } catch (Exception e) {
                throw new MojoFailureException("Unable to start", e);
            }
        }
    }

    protected void start(XmlPlexusConfiguration process) throws PluginConfigurationException, MojoFailureException, MojoExecutionException, PluginManagerException, InvalidPluginDescriptorException, PluginResolutionException, PluginDescriptorParsingException, PluginNotFoundException {

        String groupId = process.getChild("groupId").getValue(this.project.getGroupId());
        String artifactId = process.getChild("artifactId").getValue(this.project.getArtifactId());
        String executionId = process.getChild("executionId").getValue();

        MavenProject project = findProject(groupId, artifactId);

        if (project != null) {
            startProject(project, executionId, process);
            return;
        }

        Artifact artifact = findArtifact(groupId, artifactId, "swarm");

        if (artifact != null) {
            startArtifact(artifact, process);
            return;
        }

        throw new MojoFailureException("Unable to start process");
    }

    @SuppressWarnings("unchecked")
    protected void startProject(MavenProject project, String executionId, XmlPlexusConfiguration process) throws InvalidPluginDescriptorException, PluginResolutionException, PluginDescriptorParsingException, PluginNotFoundException, PluginConfigurationException, MojoFailureException, MojoExecutionException, PluginManagerException {
        Plugin plugin = this.project.getPlugin("org.wildfly.swarm:wildfly-swarm-plugin");

        Xpp3Dom config = getConfiguration(project, executionId);
        Xpp3Dom processConfig = getProcessConfiguration(process);

        Xpp3Dom globalConfig = getGlobalConfig();
        Xpp3Dom mergedConfig = Xpp3DomUtils.mergeXpp3Dom(processConfig, config);
        mergedConfig = Xpp3DomUtils.mergeXpp3Dom(mergedConfig, globalConfig);

        PluginDescriptor pluginDescriptor = this.pluginManager.loadPlugin(plugin, project.getRemotePluginRepositories(), this.repositorySystemSession);
        MojoDescriptor mojoDescriptor = pluginDescriptor.getMojo("start");
        MojoExecution mojoExecution = new MojoExecution(mojoDescriptor, mergedConfig);
        mavenSession.setCurrentProject(project);
        this.pluginManager.executeMojo(mavenSession, mojoExecution);

        List<SwarmProcess> launched = (List<SwarmProcess>) mavenSession.getPluginContext(pluginDescriptor, project).get(SWARM_PROCESS);

        List<SwarmProcess> procs = (List<SwarmProcess>) getPluginContext().get(SWARM_PROCESS);

        if (procs == null) {
            procs = new ArrayList<>();
            getPluginContext().put(SWARM_PROCESS, procs);
        }

        procs.addAll(launched);

        mavenSession.setCurrentProject(this.project);
    }

    @SuppressWarnings("unchecked")
    protected void startArtifact(Artifact artifact, XmlPlexusConfiguration process) throws InvalidPluginDescriptorException, PluginResolutionException, PluginDescriptorParsingException, PluginNotFoundException, PluginConfigurationException, MojoFailureException, MojoExecutionException, PluginManagerException {
        List<SwarmProcess> procs = (List<SwarmProcess>) getPluginContext().get(SWARM_PROCESS);

        if (procs == null) {
            procs = new ArrayList<>();
            getPluginContext().put(SWARM_PROCESS, procs);
        }

        SwarmExecutor executor = new SwarmExecutor();

        executor.withExecutableJar(artifact.getFile().toPath());

        executor.withProperties(this.properties);
        executor.withEnvironment(this.environment);

        PlexusConfiguration props = process.getChild("properties");

        for (PlexusConfiguration each : props.getChildren()) {
            executor.withProperty(each.getName(), each.getValue());
        }


        PlexusConfiguration env = process.getChild("environment");

        for (PlexusConfiguration each : env.getChildren()) {
            executor.withEnvironment(each.getName(), each.getValue());
        }
        int startTimeoutSeconds;
        try {
            startTimeoutSeconds = Integer.valueOf(props.getChild("start.timeout.seconds").getValue("30"));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Wrong format of the start timeout for " + artifact + "!. Integer expected.", nfe);
        }

        try {
            SwarmProcess launched = executor.execute();
            launched.awaitReadiness(startTimeoutSeconds, TimeUnit.SECONDS);
            procs.add(launched);
        } catch (IOException | InterruptedException e) {
            throw new MojoFailureException("Unable to execute: " + artifact, e);
        }
    }

    protected MavenProject findProject(String groupId, String artifactId) {
        if (groupId.equals(this.project.getGroupId()) && artifactId.equals(this.project.getArtifactId())) {
            return this.project;
        }
        return this.project.getCollectedProjects()
                .stream()
                .filter(e -> (e.getGroupId().equals(groupId) && e.getArtifactId().equals(artifactId)))
                .findFirst()
                .orElse(null);
    }

    protected Artifact findArtifact(String groupId, String artifactId, String classifier) {
        return this.project.getArtifacts()
                .stream()
                .filter(e -> (e.getGroupId().equals(groupId) && e.getArtifactId().equals(artifactId) && ((e.getClassifier() == null && classifier == null) || (e.getClassifier() != null && e.getClassifier().equals(classifier)))))
                .findFirst()
                .orElse(null);
    }

    protected Xpp3Dom getGlobalConfig() {
        Xpp3Dom config = new Xpp3Dom("configuration");

        Xpp3Dom properties = new Xpp3Dom("properties");
        config.addChild(properties);

        for (String name : this.properties.stringPropertyNames()) {
            Xpp3Dom prop = new Xpp3Dom(name);
            prop.setValue(this.properties.getProperty(name));
            properties.addChild(prop);
        }

        Xpp3Dom environment = new Xpp3Dom("environment");
        config.addChild(environment);

        for (String name : this.environment.stringPropertyNames()) {
            Xpp3Dom env = new Xpp3Dom(name);
            env.setValue(this.environment.getProperty(name));
            environment.addChild(env);
        }

        return config;
    }

    protected Xpp3Dom getConfiguration(MavenProject project, String executionId) {
        Plugin plugin = project.getPlugin("org.wildfly.swarm:wildfly-swarm-plugin");

        PluginExecution execution = null;

        for (PluginExecution each : plugin.getExecutions()) {
            if (executionId != null) {
                if (each.getId().equals(executionId)) {
                    execution = each;
                    break;
                }
            } else if (each.getGoals().contains("start")) {
                execution = each;
                break;
            }
        }

        Xpp3Dom config;

        if (execution == null) {
            config = new Xpp3Dom("configuration");
        } else {
            config = (Xpp3Dom) execution.getConfiguration();
        }
        Xpp3Dom pdom = new Xpp3Dom("project");
        pdom.setValue("${project}");
        config.addChild(pdom);

        pdom = new Xpp3Dom("repositorySystemSession");
        pdom.setValue("${repositorySystemSession}");
        config.addChild(pdom);

        pdom = new Xpp3Dom("remoteRepositories");
        pdom.setValue("${project.remoteArtifactRepositories}");
        config.addChild(pdom);

        return config;
    }

    protected Xpp3Dom getProcessConfiguration(XmlPlexusConfiguration process) {
        Xpp3Dom config = new Xpp3Dom("configuration");

        config.addChild(convert(process.getChild("properties")));
        config.addChild(convert(process.getChild("environment")));
        config.addChild(convert(process.getChild("jvmArguments")));

        return config;
    }


    protected Xpp3Dom convert(PlexusConfiguration config) {

        Xpp3Dom dom = new Xpp3Dom(config.getName());

        dom.setValue(config.getValue());

        for (String each : config.getAttributeNames()) {
            dom.setAttribute(each, config.getAttribute(each));
        }

        for (PlexusConfiguration each : config.getChildren()) {
            dom.addChild(convert(each));
        }

        return dom;
    }
}
