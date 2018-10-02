package org.wildfly.swarm.runner;


import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.swarm.runner.maven.CachingArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.fest.assertions.Assertions.assertThat;
import static org.wildfly.swarm.tools.ArtifactSpec.fromMavenDependencyDescription;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 8/31/18
 */
public class CachingArtifactResolvingHelperTest {

    private static CachingArtifactResolvingHelper helper;


    @BeforeClass
    public static void init() throws NoLocalRepositoryManagerException {
        helper = new CachingArtifactResolvingHelper();
    }

    @Test
    public void shouldResolveJunit() {
        ArtifactSpec spec = fromMavenDependencyDescription("junit:junit:4.12#1");
        ArtifactSpec resolved = helper.resolve(spec);
        assertThat(resolved).isNotNull();
    }

    @Test
    public void shouldResolveJunitAndAssert() throws Exception {
        List<ArtifactSpec> specs = asList(
                fromMavenDependencyDescription("junit:junit:4.12#1"),
                fromMavenDependencyDescription("org.assertj:assertj-core:3.11.1#1")
        );
        Set<ArtifactSpec> resolved = helper.resolveAll(specs, false, false);
        assertThat(resolved).hasSize(2);
    }

    @Test
    public void shouldFailToResolveGracefully() throws Exception {
        List<ArtifactSpec> specs = singletonList(fromMavenDependencyDescription("com.example:nonexistent-project:9.9.9#1"));
        Set<ArtifactSpec> resolved = helper.resolveAll(specs, false, false);
        assertThat(resolved).hasSize(0);
    }

    @Test
    public void shouldResolveJunitTransitively() throws Exception {
        List<ArtifactSpec> specs = singletonList(fromMavenDependencyDescription("junit:junit:4.12#12"));
        Set<ArtifactSpec> resolved = helper.resolveAll(specs, true, false);
        assertThat(resolved).hasSize(2);

        assertContainsArtifact(resolved, "junit", "junit", "4.12");
        assertContainsArtifact(resolved, "org.hamcrest", "hamcrest-core", "1.3");
    }

    private void assertContainsArtifact(Set<ArtifactSpec> resolved, String groupId, String artifactId, String version) {
        boolean matchFound = resolved.stream()
                .anyMatch(
                        spec ->
                                spec.groupId().equals(groupId)
                                        && spec.artifactId().equals(artifactId)
                                        && spec.version().equals(version)
                );
        assertThat(matchFound).overridingErrorMessage(String.format("%s:%s:%s has not been resolved", groupId, artifactId, version));
    }

}