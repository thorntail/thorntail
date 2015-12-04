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
package org.wildfly.swarm.plugin.maven;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
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
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.wildfly.swarm.tools.exec.SwarmProcess;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(name = "multistart",
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MultiStartMojo extends AbstractMojo {

    @Component
    protected MavenProject project;

    @Parameter(alias = "properties")
    private Properties properties;

    @Parameter
    private Properties environment;

    @Parameter(alias = "environmentFile")
    private File environmentFile;

    @Parameter(alias = "processes")
    private List<XmlPlexusConfiguration> processes;

    //  ----------------------------------------
    @Parameter(defaultValue = "${session}")
    private MavenSession mavenSession;

    @Parameter(defaultValue = "${repositorySystemSession}")
    protected DefaultRepositorySystemSession repositorySystemSession;

    @Component
    private BuildPluginManager pluginManager;

    @Parameter(defaultValue = "${mojoExecution}")
    private MojoExecution mojoExecution;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.properties == null) {
            this.properties = new Properties();
        }
        if (this.environment == null) {
            this.environment = new Properties();
        }
        if (environmentFile != null) {
            Properties ef = new Properties();
            try {
                Reader inStream = new FileReader(environmentFile);
                ef.load(inStream);
                inStream.close();
                this.environment.putAll(ef);
            } catch (IOException e) {
                getLog().error("env file not found or not parsable " + environmentFile);
            }
        }

        //System.err.println("collected: " + this.project.getCollectedProjects());

        //System.err.println( "processes: " + this.processes );

        /*
        for (MavenProject each : this.project.getCollectedProjects()) {
            Plugin plugin = each.getPlugin("org.wildfly.swarm:wildfly-swarm-plugin");
            for (PluginExecution exec : plugin.getExecutions()) {
                if (exec.getGoals().contains("start") ) {
                    Xpp3Dom config = (Xpp3Dom) exec.getConfiguration();
                    System.err.println("config: " + config.getClass() + " // " + config);
                    System.err.println( "maven session: " + mavenSession );
                    System.err.println( "maven session.proj: " + mavenSession.getCurrentProject() );
                    System.err.println( "pluginManager: " + this.pluginManager );

                    Xpp3Dom pdom = new Xpp3Dom("project");
                    pdom.setValue( "${project}" );
                    config.addChild( pdom );

                    try {
                        PluginDescriptor pluginDescriptor = this.pluginManager.loadPlugin(plugin, each.getRemotePluginRepositories(), this.repositorySystemSession);
                        MojoDescriptor mojoDescriptor = pluginDescriptor.getMojo("start");
                        MojoExecution mojoExecution = new MojoExecution( mojoDescriptor, config );
                        //MojoExecution mojoExecution = new MojoExecution( plugin, "start", "start" );
                        mavenSession.setCurrentProject( each );
                        this.pluginManager.executeMojo(mavenSession, mojoExecution);
                    } catch (Throwable t) {
                        while ( t != null ) {
                            t.printStackTrace();
                            t = t.getCause();
                        }
                    }
                }
            }
        }
        */

        for (XmlPlexusConfiguration process : this.processes) {
            try {
                start(process);
            } catch (Exception e) {
                throw new MojoFailureException("Unable to start", e);
            }
        }
    }

    protected void start(XmlPlexusConfiguration process) throws PluginConfigurationException, MojoFailureException, MojoExecutionException, PluginManagerException, InvalidPluginDescriptorException, PluginResolutionException, PluginDescriptorParsingException, PluginNotFoundException {
        Plugin plugin = this.project.getPlugin("org.wildfly.swarm:wildfly-swarm-plugin");

        String groupId = process.getChild("groupId").getValue(this.project.getGroupId());
        String artifactId = process.getChild("artifactId").getValue();
        String executionId = process.getChild("executionId").getValue();

        MavenProject project = findProject(groupId, artifactId);

        Xpp3Dom config = getConfiguration(project, executionId);
        Xpp3Dom processConfig = getProcessConfiguration(process);

        Xpp3Dom mergedConfig = Xpp3DomUtils.mergeXpp3Dom(processConfig, config);

        PluginDescriptor pluginDescriptor = this.pluginManager.loadPlugin(plugin, project.getRemotePluginRepositories(), this.repositorySystemSession);
        MojoDescriptor mojoDescriptor = pluginDescriptor.getMojo("start");
        MojoExecution mojoExecution = new MojoExecution(mojoDescriptor, mergedConfig);
        mavenSession.setCurrentProject(project);
        this.pluginManager.executeMojo(mavenSession, mojoExecution);

        List<SwarmProcess> launched = (List<SwarmProcess>) mavenSession.getPluginContext( pluginDescriptor, project ).get("swarm-process" );

        System.err.println( "launched: " + launched );

        List<SwarmProcess> procs = (List<SwarmProcess>) getPluginContext().get("swarm-process");

        if ( procs == null ) {
            procs = new ArrayList<>();
            getPluginContext().put( "swarm-process", procs );
        }

        procs.addAll( launched );

        mavenSession.setCurrentProject(this.project);
    }

    protected MavenProject findProject(String groupId, String artifactId) {
        return this.project.getCollectedProjects()
                .stream()
                .filter(e -> (e.getGroupId().equals(groupId) && e.getArtifactId().equals(artifactId)))
                .findFirst()
                .orElse(null);
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

        Xpp3Dom config = (Xpp3Dom) execution.getConfiguration();
        Xpp3Dom pdom = new Xpp3Dom("project");
        pdom.setValue("${project}");
        config.addChild(pdom);

        return config;
    }

    protected Xpp3Dom getProcessConfiguration(XmlPlexusConfiguration process) {
        Xpp3Dom config = new Xpp3Dom("configuration");

        config.addChild(convert(process.getChild("properties")));
        config.addChild(convert(process.getChild("environment")));

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
