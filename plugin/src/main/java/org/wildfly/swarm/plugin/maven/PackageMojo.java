package org.wildfly.swarm.plugin.maven;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.impl.ArtifactResolver;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.BuildTool;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(
        name = "package",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class PackageMojo extends AbstractMojo { //extends AbstractSwarmMojo {


    @Component
    protected MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${repositorySystemSession}")
    protected DefaultRepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}")
    protected List<ArtifactRepository> remoteRepositories;

    @Inject
    private ArtifactResolver resolver;

    @Parameter(alias = "modules")
    private String[] additionalModules;

    @Parameter(alias = "bundleDependencies", defaultValue = "true")
    private boolean bundleDependencies;

    @Parameter(alias = "mainClass")
    private String mainClass;

    @Parameter(alias = "httpPort")
    private Integer httpPort;

    @Parameter(alias = "portOffset")
    private Integer portOffset;

    @Parameter(alias = "bindAddress")
    private String bindAddress;

    @Parameter(alias = "contextPath", defaultValue = "/")
    private String contextPath;

    @Parameter(alias = "properties")
    private Properties properties;

    @Parameter(alias = "propertiesFile")
    private String propertiesFile;

    private BuildTool tool;

    protected Properties loadProperties(File file) throws MojoFailureException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);
        } catch (FileNotFoundException e) {
            throw new MojoFailureException("No such file: " + file, e);
        } catch (IOException e) {
            throw new MojoFailureException("Error reading file: " + file, e);
        }

        return props;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (this.properties == null) {
            this.properties = new Properties();
        }

        if (propertiesFile != null) {
            File propsFile = new File(this.propertiesFile);
            this.properties.putAll(loadProperties(propsFile));
        }

        if ( this.httpPort != null ) {
            getLog().warn( "<httpPort> is deprecated, please use <jboss.http.port> within <properties>");
            this.properties.setProperty( "jboss.http.port", this.httpPort.toString() );
        }

        if ( this.portOffset != null ) {
            getLog().warn( "<portOffset> is deprecated, please use <jboss.port.offset> within <properties>" );
            this.properties.setProperty( "jboss.port.offset", this.portOffset.toString() );
        }

        if ( this.bindAddress != null ) {
            getLog().warn( "<bindAddress> is deprecated, please use <jboss.bind.address> within <properties>" );
            this.properties.setProperty( "jboss.bind.address", this.bindAddress );
        }


        this.tool = new BuildTool();

        this.tool.projectArtifact(
                this.project.getArtifact().getGroupId(),
                this.project.getArtifact().getArtifactId(),
                this.project.getArtifact().getVersion(),
                this.project.getArtifact().getType(),
                this.project.getArtifact().getFile());

        Set<Artifact> deps = this.project.getArtifacts();
        for (Artifact each : deps) {
            this.tool.dependency(each.getScope(), each.getGroupId(), each.getArtifactId(), each.getVersion(), each.getType(), each.getClassifier(), each.getFile());
        }

        List<Resource> resources = this.project.getResources();
        for (Resource each : resources) {
            this.tool.resourceDirectory(each.getDirectory());
        }
        
        
        if (additionalModules == null) {
            additionalModules = new String[] {"modules"};
        }
        
        for (String additionalModule : additionalModules) {
            File source = new File(this.project.getBuild().getOutputDirectory() + File.separator + additionalModule);
            if (source.exists()) {
                this.tool.additionnalModules().add(source.getAbsolutePath());
            }
        }

        this.tool
                .properties(this.properties)
                .mainClass(this.mainClass)
                .contextPath(this.contextPath);

        MavenArtifactResolvingHelper resolvingHelper = new MavenArtifactResolvingHelper(this.resolver, this.repositorySystemSession);
        for (ArtifactRepository each : this.remoteRepositories) {
            resolvingHelper.remoteRepository(each);
        }

        this.tool.artifactResolvingHelper(resolvingHelper);

        try {
            File jar = this.tool.build(this.project.getBuild().getFinalName(), Paths.get( this.projectBuildDir ));

            Artifact primaryArtifact = this.project.getArtifact();

            ArtifactHandler handler = new DefaultArtifactHandler("jar");
            Artifact swarmJarArtifact = new DefaultArtifact(
                    primaryArtifact.getGroupId(),
                    primaryArtifact.getArtifactId(),
                    primaryArtifact.getVersion(),
                    primaryArtifact.getScope(),
                    "jar",
                    "swarm",
                    handler
            );

            swarmJarArtifact.setFile(jar);

            this.project.addAttachedArtifact(swarmJarArtifact);
        } catch (Exception e) {
            throw new MojoFailureException("Unable to create -swarm.jar", e);
        }
    }

}

