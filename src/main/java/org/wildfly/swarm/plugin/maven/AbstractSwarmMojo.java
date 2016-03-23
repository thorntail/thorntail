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
package org.wildfly.swarm.plugin.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.PropertiesUtil;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSwarmMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected DefaultRepositorySystemSession repositorySystemSession;

    @Parameter(alias = "remoteRepositories", defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    protected List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Parameter(alias = "mainClass")
    protected String mainClass;

    @Parameter(alias = "properties")
    protected Properties properties;

    @Parameter(alias = "propertiesFile")
    protected String propertiesFile;

    @Parameter(alias = "environment")
    protected Properties environment;

    @Parameter(alias = "environmentFile")
    protected String environmentFile;

    @Parameter(alias = "modules")
    protected List<String> additionalModules = new ArrayList<>();

    @Parameter(alias = "fractions")
    protected List<String> additionalFractions = new ArrayList<>();

    @Inject
    protected ArtifactResolver resolver;

    @Component
    protected RepositorySystem repositorySystem;

    AbstractSwarmMojo() {
        if (this.additionalModules.isEmpty()) {
            this.additionalModules.add("modules");
        }
    }

    protected void initProperties(final boolean withMaven) {
        if (this.properties == null) {
            this.properties = new Properties();
        }

        if (this.propertiesFile != null) {
            try {
                this.properties.putAll(PropertiesUtil.loadProperties(this.propertiesFile));
            } catch (IOException e) {
                getLog().error("Failed to load properties from " + this.propertiesFile, e);
            }
        }

        this.properties.putAll(PropertiesUtil.filteredSystemProperties(this.properties, withMaven));
    }

    protected void initEnvironment() throws MojoFailureException {
        if (this.environment == null) {
            this.environment = new Properties();
        }
        if (this.environmentFile != null) {
            try {
                this.environment.putAll(PropertiesUtil.loadProperties(this.environmentFile));
            } catch (IOException e) {
                getLog().error("Failed to load environment from " + this.environmentFile, e);
            }
        }
    }

    // borrowed from https://github.com/forge/furnace/blob/master/manager/resolver/maven/src/main/java/org/jboss/forge/furnace/manager/maven/MavenContainer.java#L216
    public static org.eclipse.aether.repository.Proxy convertFromMavenProxy(Proxy proxy) {
        if (proxy != null) {
            return  new org.eclipse.aether.repository.Proxy(proxy.getProtocol(),
                                                            proxy.getHost(),
                                                            proxy.getPort(),
                                                            new AuthenticationBuilder()
                                                                    .addUsername(proxy.getUsername())
                                                                    .addPassword(proxy.getPassword())
                                                                    .build());
        }

        return null;
    }
    protected MavenArtifactResolvingHelper mavenArtifactResolvingHelper() {
        MavenArtifactResolvingHelper resolvingHelper =
                new MavenArtifactResolvingHelper(this.resolver,
                                                 this.repositorySystem,
                                                 this.repositorySystemSession,
                                                 convertFromMavenProxy(this.mavenSession.getSettings().getActiveProxy()));
        this.remoteRepositories.forEach(resolvingHelper::remoteRepository);

        return resolvingHelper;
    }

    protected ArtifactSpec artifactToArtifactSpec(Artifact dep) {
        return new ArtifactSpec(dep.getScope(),
                                dep.getGroupId(),
                                dep.getArtifactId(),
                                dep.getBaseVersion(),
                                dep.getType(),
                                dep.getClassifier(),
                                dep.getFile());
    }
}
