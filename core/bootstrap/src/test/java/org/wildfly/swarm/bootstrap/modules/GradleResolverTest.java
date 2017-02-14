package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test {@link GradleResolver}
 *
 * @author Michael Fraefel
 */
public class GradleResolverTest {

    @Test
    public void testToGradleArtifactFileName(){
        //GIVEN
        String group = "org.wildfly.swarm";
        String packaging = "jar";
        String artifact = "test";
        String version = "1.0";
        ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(group, artifact, version);

        //WHEN
        GradleResolver resolver = new GradleResolver(null);
        String artifactFileName = resolver.toGradleArtifactFileName(artifactCoordinates, packaging);

        //THEN
        assertEquals(artifact + "-" + version + "." + packaging, artifactFileName);
    }

    @Test
    public void testToGradleArtifactFileName_withClassifier(){
        //GIVEN
        String group = "org.wildfly.swarm";
        String packaging = "jar";
        String artifact = "test";
        String version = "1.0";
        String classifier = "sources";
        ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(group, artifact, version, classifier);

        //WHEN
        GradleResolver resolver = new GradleResolver(null);
        String artifactFileName = resolver.toGradleArtifactFileName(artifactCoordinates, packaging);

        //THEN
        assertEquals(artifact + "-" + version + "-"+ classifier + "." + packaging, artifactFileName);
    }

    @Test
    public void testResolveArtifact() throws IOException {
        //GIVEN
        Path gradleCachePath = Files.createTempDirectory("gradle");
        String group = "org.wildfly.swarm";
        String packaging = "jar";
        String artifact = "test";
        String version = "1.0";
        String classifier = "sources";
        ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(group, artifact, version, classifier);

        Path artifactDir = Files.createDirectories(gradleCachePath.resolve(group).resolve(artifact).resolve(version).resolve("hash"));
        File artifactFile = Files.createFile(artifactDir.resolve(artifact + "-" + version + "-" + classifier + "." + packaging)).toFile();

        //WHEN
        GradleResolver resolver = new GradleResolver(gradleCachePath.toString());
        File resolvedArtifactFile = resolver.resolveArtifact(artifactCoordinates, packaging);

        //THEN
        assertEquals(artifactFile, resolvedArtifactFile);
    }

    @Test
    public void testResolveArtifact_notExists() throws IOException {
        //GIVEN
        Path gradleCachePath = Files.createTempDirectory("gradle");
        String group = "org.wildfly.swarm";
        String packaging = "jar";
        String artifact = "test";
        String version = "1.0";
        String classifier = "sources";
        ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(group, artifact, version, classifier);

        Path artifactDir = Files.createDirectories(gradleCachePath.resolve(group).resolve(artifact).resolve(version).resolve("hash"));
        File artifactFile = Files.createFile(artifactDir.resolve(artifact + "-" + version + "-" + classifier + ".pom")).toFile(); // Other packaging type

        //WHEN
        GradleResolver resolver = new GradleResolver(gradleCachePath.toString());
        File resolvedArtifactFile = resolver.resolveArtifact(artifactCoordinates, packaging);

        //THEN
        assertNull(resolvedArtifactFile);
    }

}
