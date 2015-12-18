package org.wildfly.swarm.plugin.maven;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.impl.ArtifactResolver;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSwarmMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected DefaultRepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    protected List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Inject
    protected ArtifactResolver resolver;

    @Parameter(alias = "modules")
    protected String[] additionalModules;

    @Parameter(alias = "bundleDependencies", defaultValue = "true")
    protected boolean bundleDependencies;

    @Parameter(alias = "mainClass")
    protected String mainClass;

    @Parameter(alias = "httpPort")
    protected Integer httpPort;

    @Parameter(alias = "portOffset")
    protected Integer portOffset;

    @Parameter(alias = "bindAddress")
    protected String bindAddress;

    @Parameter(alias = "contextPath", defaultValue = "/")
    protected String contextPath;

    @Parameter(alias = "properties")
    protected Properties properties;

    @Parameter(alias = "propertiesFile")
    protected String propertiesFile;

    @Parameter(alias = "environment")
    protected Properties environment;

    @Parameter(alias = "environmentFile")
    protected String environmentFile;

}
