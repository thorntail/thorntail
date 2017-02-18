package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test {@link GradleResolver}
 *
 * @author Michael Fraefel
 */
public class GradleResolverTest {


    @Test
    public void downloadFromRemoteRepository() throws IOException {
        //GIVEN
        Path gradleCachePath = Files.createTempDirectory(".gradle");
        String group = "org.wildfly.swarm";
        String packaging = "jar";
        String artifact = "bootstrap";
        String version = "2017.1.1";
        ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(group, artifact, version);

        Path artifactDirectory = gradleCachePath.resolve(group);
        GradleResolver resolver = spy(new GradleResolver(gradleCachePath.toString()));
        File targetFile = mock(File.class);
        doReturn(targetFile).when(resolver).doDownload(anyString(), anyString(), anyString(), eq(artifactCoordinates), eq(packaging), any(File.class), any(File.class));

        //WHEN
        File result = resolver.downloadFromRemoteRepository(artifactCoordinates, packaging, artifactDirectory);

        //THEN
        assertEquals(targetFile, result);
    }


    @Test
    public void downloadFromRemoteRepository_unknown() throws IOException {
        //GIVEN
        Path gradleCachePath = Files.createTempDirectory(".gradle");
        String group = "org.wildfly.swarm";
        String packaging = "jar";
        String artifact = "test";
        String version = "2017.1.1";
        ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(group, artifact, version);

        Path artifactDirectory = gradleCachePath.resolve(group);
        GradleResolver resolver = spy(new GradleResolver(gradleCachePath.toString()));
        doReturn(null).when(resolver).doDownload(anyString(), anyString(), anyString(), eq(artifactCoordinates), eq(packaging), any(File.class), any(File.class));

        //WHEN
        File result = resolver.downloadFromRemoteRepository(artifactCoordinates, packaging, artifactDirectory);

        //THEN
        assertNull(result);
    }

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
    public void testResolveArtifact_latest() throws IOException, InterruptedException {
        //GIVEN
        Path gradleCachePath = Files.createTempDirectory("gradle");
        String group = "org.wildfly.swarm";
        String packaging = "jar";
        String artifact = "test";
        String version = "1.0";
        String classifier = "sources";
        ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(group, artifact, version, classifier);

        Path artifactDir = Files.createDirectories(gradleCachePath.resolve(group).resolve(artifact).resolve(version).resolve("hash1"));
        File artifactFile = Files.createFile(artifactDir.resolve(artifact + "-" + version + "-" + classifier + "." + packaging)).toFile();
        Thread.sleep(2000); //Timestemp resolution of some filesystems are 2 seconds
        Path artifactDirLatest = Files.createDirectories(gradleCachePath.resolve(group).resolve(artifact).resolve(version).resolve("hash2"));
        File artifactFileLatest = Files.createFile(artifactDirLatest.resolve(artifact + "-" + version + "-" + classifier + "." + packaging)).toFile();

        //WHEN
        GradleResolver resolver = new GradleResolver(gradleCachePath.toString());
        File resolvedArtifactFile = resolver.resolveArtifact(artifactCoordinates, packaging);

        //THEN
        assertEquals(artifactFileLatest, resolvedArtifactFile);
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
