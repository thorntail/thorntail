package org.wildfly.swarm.plugin.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.DefaultExcludeRule;
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class GradleArtifactResolvingHelper implements ArtifactResolvingHelper {


    private final Project project;

    public GradleArtifactResolvingHelper(Project project) {
        this.project = project;
        this.project.getRepositories().maven(new Action<MavenArtifactRepository>() {
            @Override
            public void execute(MavenArtifactRepository repo) {
                repo.setName("jboss-public");
                repo.setUrl("http://repository.jboss.org/nexus/content/groups/public/");
            }
        });
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

    @Override
    public Set<ArtifactSpec> resolveAll(Set<ArtifactSpec> specs) throws Exception {
        // TODO: determine if we need to implement this. Current usage of BuildTool doesn't need it for gradle
        throw new UnsupportedOperationException("Not implemented");
    }


}
