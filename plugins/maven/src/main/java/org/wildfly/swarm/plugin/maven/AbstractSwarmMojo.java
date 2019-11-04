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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.ArtifactResolver;
import org.wildfly.swarm.fractions.PropertiesUtil;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.DeclaredDependencies;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSwarmMojo extends AbstractMojo {

    protected static final String EXCLUDE_PREFIX = "!";

    protected static final String JAR = "jar";

    protected static final String WAR = "war";

    protected static final String DOT = ".";

    protected static final String JAR_FILE_EXTENSION = DOT + JAR;

    protected static final String WAR_FILE_EXTENSION = DOT + WAR;

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

    @Deprecated
    @Parameter(alias = "mainClass", property = "swarm.mainClass")
    protected String mainClass;

    /**
     * Flag to skip all executions
     */
    @Parameter(alias = "skipAll", defaultValue = "false", property = "thorntail.skipAll")
    protected boolean skipAll;

    @Parameter(alias = "properties")
    protected Properties properties;

    @Parameter(alias = "propertiesFile", property = "thorntail.propertiesFile")
    protected String propertiesFile;

    @Parameter(alias = "environment")
    protected Properties environment;

    @Parameter(alias = "environmentFile", property = "thorntail.environmentFile")
    protected String environmentFile;

    @Parameter(alias = "modules")
    protected List<String> additionalModules = new ArrayList<>();

    @Parameter(alias = "fractions")
    protected List<String> fractions = new ArrayList<>();

    @Parameter(defaultValue = "when_missing", property = "thorntail.detect.mode")
    protected BuildTool.FractionDetectionMode fractionDetectMode;

    @Inject
    protected ArtifactResolver resolver;

    @Component
    protected RepositorySystem repositorySystem;

    AbstractSwarmMojo() {
        if (this.additionalModules.isEmpty()) {
            this.additionalModules.add("modules");
        }
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        deprecationWarnings();
        if (this.skipAll) {
            getLog().info("Skipping thorntail-maven-plugin execution");
            return;
        }
        executeSpecific();
    }

    public abstract void executeSpecific() throws MojoExecutionException, MojoFailureException;

    protected void deprecationWarnings() {
        if (mainClass != null && !mainClass.equals("")) {
            getLog().warn(
                    "\n------\n" +
                    "Custom main() usage is deprecated and is no longer supported.\n" +
                    "Please refer to https://docs.thorntail.io for YAML configuration that replaces it." +
                    "\n------"
            );
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

    protected MavenArtifactResolvingHelper mavenArtifactResolvingHelper() {
        MavenArtifactResolvingHelper resolvingHelper =
                new MavenArtifactResolvingHelper(this.resolver,
                                                 this.repositorySystem,
                                                 this.repositorySystemSession,
                                                 this.project.getDependencyManagement());
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

    protected Map<ArtifactSpec, Set<ArtifactSpec>> createBuckets(Set<Artifact> transientDeps, List<Dependency> directDeps) {
        Map<ArtifactSpec, Set<ArtifactSpec>> buckets = new HashMap<>();
        for (Artifact dep : transientDeps) {
            if (dep.getDependencyTrail().isEmpty()) {
                throw new RuntimeException("Empty trail " + asBucketKey(dep));
            } else if (dep.getDependencyTrail().size() == 2) {
                ArtifactSpec key = asBucketKey(dep);
                //System.out.println("Appears to be top level: "+ key);
                if (!buckets.containsKey(key)) {
                    buckets.put(key, new HashSet<>());
                }
            } else {

                String owner = dep.getDependencyTrail().get(1);
                String ownerScope = null;
                String[] tokens = owner.split(":");
                for (Dependency d : directDeps) {
                    if (d.getGroupId().equals(tokens[0])
                            && d.getArtifactId().equals(tokens[1])) {
                        ownerScope = d.getScope();
                        break;
                    }
                }

                assert ownerScope != null : "Failed to resolve owner scope";

                ArtifactSpec parent = DeclaredDependencies.createSpec(owner, ownerScope);
                if (!buckets.containsKey(parent)) {
                    buckets.put(parent, new HashSet<>());
                }
                buckets.get(parent).add(asBucketKey(dep));
            }
        }
        return buckets;
    }

    private static ArtifactSpec asBucketKey(Artifact artifact) {

        return new ArtifactSpec(
                artifact.getScope(),
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getBaseVersion(),
                artifact.getType(),
                artifact.getClassifier(),
                artifact.getFile()
        );
    }

}
