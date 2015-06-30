package org.wildfly.swarm.plugin;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

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
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
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

    @Parameter(alias = "httpPort", defaultValue = "8080")
    private int httpPort;

    @Parameter(alias = "portOffset", defaultValue = "0")
    private int portOffset;

    @Parameter(alias = "bindAddress", defaultValue = "0.0.0.0")
    private String bindAddress;

    @Parameter(alias = "contextPath", defaultValue = "/")
    private String contextPath;




    private BuildTool tool;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.tool = new BuildTool( Paths.get( this.projectBuildDir ) );

        this.tool.projectArtifact(
                this.project.getArtifact().getGroupId(),
                this.project.getArtifact().getArtifactId(),
                this.project.getArtifact().getVersion(),
                this.project.getArtifact().getType(),
                this.project.getArtifact().getFile());

        Set<Artifact> deps = this.project.getArtifacts();
        for ( Artifact each : deps ) {
            this.tool.dependency(each.getScope(), each.getGroupId(), each.getArtifactId(), each.getVersion(), each.getType(), each.getClassifier(), each.getFile());
        }

        List<Resource> resources = this.project.getResources();
        for ( Resource each : resources ) {
           this.tool.resourceDirectory( each.getDirectory() );
        }

        this.tool
                .mainClass( this.mainClass )
                .contextPath( this.contextPath )
                .portOffset( this.portOffset )
                .httpPort( this.httpPort )
                .bindAddress( this.bindAddress );

        this.tool.artifactResolvingHelper( new MavenArtifactResolvingHelper( this.resolver, this.repositorySystemSession, this.remoteRepositories ) );

        try {
            File jar = this.tool.build( this.project.getBuild().getFinalName() );

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

            swarmJarArtifact.setFile( jar );

            this.project.addAttachedArtifact( swarmJarArtifact );
        } catch (Exception e) {
            throw new MojoFailureException("Unable to create -swarm.jar", e);
        }
    }

}
