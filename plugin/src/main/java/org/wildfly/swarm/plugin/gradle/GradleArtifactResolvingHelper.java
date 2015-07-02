package org.wildfly.swarm.plugin.gradle;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.internal.artifacts.DefaultExcludeRule;
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class GradleArtifactResolvingHelper implements ArtifactResolvingHelper {


    private final Project project;

    public GradleArtifactResolvingHelper(Project project) {
        this.project = project;
    }


    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) {
        if (spec.file != null) {
            return spec;
        }

        Configuration config = this.project.getConfigurations().detachedConfiguration();

        DefaultExternalModuleDependency d = new DefaultExternalModuleDependency(spec.groupId, spec.artifactId, spec.version);
        DefaultDependencyArtifact da = new DefaultDependencyArtifact(spec.artifactId, spec.packaging, spec.packaging, spec.classifier, null);
        d.addArtifact(da);
        d.getExcludeRules().add(new DefaultExcludeRule());
        config.getDependencies().add(d);

        Set<ResolvedDependency> resolved = config.getResolvedConfiguration().getFirstLevelModuleDependencies();
        for (ResolvedDependency eachDep : resolved) {
            Set<ResolvedArtifact> artifacts = eachDep.getModuleArtifacts();
            for (ResolvedArtifact eachArtifact : artifacts) {
                spec.file = eachArtifact.getFile();
                return spec;
            }
        }
        return null;
    }


}
