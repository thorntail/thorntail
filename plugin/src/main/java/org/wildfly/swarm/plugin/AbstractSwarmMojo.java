package org.wildfly.swarm.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * @author Ken Finnigan
 */
public abstract class AbstractSwarmMojo extends AbstractMojo {

    protected static final String MODULE_PREFIX = "modules/system/layers/base/";
    protected static final String MODULE_SUFFIX = "/main/module.xml";
    protected static final String TARGET_NAME_PREFIX = "target-name=\"";

    @Component
    protected MavenProject project;

    @Component
    protected ProjectBuilder projectBuilder;

    @Component
    protected ProjectDependenciesResolver projectDependenciesResolver;

    @Parameter(defaultValue = "${session}")
    protected MavenSession session;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${repositorySystemSession}")
    protected DefaultRepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}")
    protected List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "${localRepository}")
    protected ArtifactRepository localRepository;

    @Parameter(defaultValue = "${plugin.artifacts}")

    protected List<Artifact> pluginArtifacts;
    protected Set<Artifact> featurePacks = new HashSet<>();
    protected Set<Artifact> featurePackArtifacts = new HashSet<>();
    protected Set<ArtifactSpec> gavs = new HashSet<>();

    protected void setupFeaturePacks(ArtifactResolver resolver) throws MojoFailureException {
        Set<Artifact> artifacts = this.project.getArtifacts();

        for (Artifact each : artifacts) {
            String featurePackReference = getFeaturePackReference(each);
            if (featurePackReference != null) {
                Artifact resultArtifact = resolveFeaturePack(resolver, featurePackReference);
                if (resultArtifact != null) {
                    this.featurePacks.add(resultArtifact);
                } else {
                    throw new MojoFailureException("Unable to resolve referenced fraction: " + featurePackReference);
                }
            }
        }
    }

    protected Artifact resolveFeaturePack(ArtifactResolver resolver, String fractionGav) throws MojoFailureException {
        String[] parts = fractionGav.split(":");
        if ( parts.length != 4 ) {
            throw new MojoFailureException( "wildfly-swarm-feature-pack.gav must contain 4 segments" );
        }
        ArtifactRequest request = new ArtifactRequest();
        org.eclipse.aether.artifact.DefaultArtifact aetherArtifact
                = new org.eclipse.aether.artifact.DefaultArtifact(parts[0], parts[1], parts[2], parts[3]);
        request.setArtifact(aetherArtifact);
        request.setRepositories(remoteRepositories());

        try {
            ArtifactResult result = resolver.resolveArtifact(this.repositorySystemSession, request);
            return convertAetherToMavenArtifact( result.getArtifact(), "compile", "zip" );
        } catch (ArtifactResolutionException e) {
            throw new MojoFailureException( "Unable to resolve fraction: " + fractionGav, e );
        }
    }

    protected String getFeaturePackReference(Artifact artifact) throws MojoFailureException {
        if (artifact.getType().equals("jar")) {
            try {
                ZipFile zip = new ZipFile(artifact.getFile());
                ZipEntry entry = zip.getEntry("wildfly-swarm-feature-pack.gav");
                if (entry == null) {
                    return null;
                }
                InputStream in = zip.getInputStream(entry);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                try {
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.equals("") || line.startsWith("//")) {
                            continue;
                        }
                        return line;
                    }
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                throw new MojoFailureException("Unable to inspect dependency: " + artifact, e);
            }
        }
        return null;
    }

    protected void processFractions(ArtifactResolver resolver, ExceptionConsumer<org.eclipse.aether.artifact.Artifact> fractionHandler) throws MojoFailureException {
        Set<Artifact> artifacts = this.project.getArtifacts();

        List<org.eclipse.aether.artifact.Artifact> fractions = new ArrayList<>();

        for (Artifact each : artifacts) {
            String fractionReference = getFractionReference(each);
            if (fractionReference != null) {
                org.eclipse.aether.artifact.Artifact resultArtifact = resolveFraction(resolver, fractionReference);
                if (resultArtifact != null) {
                    fractions.add(resultArtifact);
                } else {
                    throw new MojoFailureException("Unable to resolve referenced fraction: " + fractionReference);
                }
            }
        }

        try {
            for (org.eclipse.aether.artifact.Artifact artifact : fractions) {
                fractionHandler.accept(artifact);
            }
        } catch (Exception e) {
            throw new MojoFailureException("Unable to process fractions", e);
        }
    }

    protected String getFractionReference(Artifact artifact) throws MojoFailureException {
        if (artifact.getType().equals("jar")) {
            try {
                ZipFile zip = new ZipFile(artifact.getFile());
                ZipEntry entry = zip.getEntry("wildfly-swarm-fraction.gav");
                if (entry == null) {
                    return null;
                }
                InputStream in = zip.getInputStream(entry);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                try {
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.equals("") || line.startsWith("//")) {
                            continue;
                        }
                        return line;
                    }
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                throw new MojoFailureException("Unable to inspect dependency: " + artifact, e);
            }
        }
        return null;
    }

    protected org.eclipse.aether.artifact.Artifact resolveFraction(ArtifactResolver resolver, String fractionGav) throws MojoFailureException {
        String[] parts = fractionGav.split(":");
        if ( parts.length != 5 ) {
            throw new MojoFailureException( "wildfly-swarm-fraction.gav must contain 5 segments" );
        }
        ArtifactRequest request = new ArtifactRequest();
        org.eclipse.aether.artifact.DefaultArtifact aetherArtifact
                = new org.eclipse.aether.artifact.DefaultArtifact(parts[0], parts[1], parts[3], parts[2], parts[4]);
        request.setArtifact(aetherArtifact);
        request.setRepositories(remoteRepositories());

        try {
            ArtifactResult result = resolver.resolveArtifact(this.repositorySystemSession, request);
            return result.getArtifact();
        } catch (ArtifactResolutionException e) {
            throw new MojoFailureException( "Unable to resolve fraction: " + fractionGav, e );
        }
    }

    protected ProjectBuildingResult buildProject(Artifact artifact) throws Exception {
        return this.projectBuilder.build(artifact,
                new DefaultProjectBuildingRequest()
                        .setLocalRepository(localRepository)
                        .setRemoteRepositories(remoteRepositories)
                        .setRepositorySession(repositorySystemSession)
        );
    }

    protected DependencyResolutionResult resolveProjectDependencies(MavenProject project, DependencyFilter filter) throws Exception {
        return this.projectDependenciesResolver.resolve(
                new DefaultDependencyResolutionRequest()
                        .setRepositorySession(session.getRepositorySession())
                        .setMavenProject(project)
                        .setResolutionFilter(filter)
        );
    }

    protected Artifact convertAetherToMavenArtifact(org.eclipse.aether.artifact.Artifact aetherArtifact, String scope, String handlerType) {
        Artifact artifact = new DefaultArtifact(aetherArtifact.getGroupId(),
                aetherArtifact.getArtifactId(),
                aetherArtifact.getVersion(),
                scope,
                aetherArtifact.getExtension(),
                aetherArtifact.getClassifier(),
                new DefaultArtifactHandler(handlerType));
        artifact.setFile(aetherArtifact.getFile());
        return artifact;
    }

    protected List<RemoteRepository> remoteRepositories() {
        List<RemoteRepository> repos = new ArrayList<>();

        for (ArtifactRepository each : this.remoteRepositories) {
            RemoteRepository.Builder builder = new RemoteRepository.Builder(each.getId(), "default", each.getUrl());
            repos.add(builder.build());
        }

        return repos;
    }

    protected void copyFileFromZip(ZipFile resource, ZipEntry entry, File outFile) throws IOException {
        InputStream in = resource.getInputStream(entry);
        try {
            FileOutputStream out = new FileOutputStream(outFile);
            try {
                copyContent(in, out);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    protected void copyContent(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len = -1;

        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
    }

    protected interface ExceptionConsumer<T> {
        void accept(T t) throws Exception;
    }
}
