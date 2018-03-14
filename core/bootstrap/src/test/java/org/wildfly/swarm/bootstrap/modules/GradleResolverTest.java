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
package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.junit.After;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

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

    @After
    public void tearDown() {
        TempFileManager.INSTANCE.close();
    }

    @Test
    public void downloadFromRemoteRepository() throws IOException {
        //GIVEN
        File dirFile = TempFileManager.INSTANCE.newTempDirectory(".gradle", null);
        Path gradleCachePath = dirFile.toPath();
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
        File dirFile = TempFileManager.INSTANCE.newTempDirectory(".gradle", null);
        Path gradleCachePath = dirFile.toPath();
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
    public void testToGradleArtifactPath(){
        //GIVEN
        String group = "org.jboss.ws.cxf";
        String artifact = "jbossws-cxf-resources";
        String version = "5.1.5.Final";
        ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(group, artifact, version);

        //WHEN
        GradleResolver resolver = new GradleResolver(null);
        String artifactPath = resolver.toGradleArtifactPath(artifactCoordinates);

        //THEN
        assertEquals(
                "org/jboss/ws/cxf/jbossws-cxf-resources/5.1.5.Final/jbossws-cxf-resources-5.1.5.Final",
                artifactPath);
    }

    @Test
    public void testToGradleArtifactPath_withClassifier(){
        //GIVEN
        String group = "org.jboss.ws.cxf";
        String artifact = "jbossws-cxf-resources";
        String version = "5.1.5.Final";
        String classifier = "wildfly1000";
        ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(group, artifact, version, classifier);

        //WHEN
        GradleResolver resolver = new GradleResolver(null);
        String artifactPath = resolver.toGradleArtifactPath(artifactCoordinates);

        //THEN
        assertEquals(
                "org/jboss/ws/cxf/jbossws-cxf-resources/5.1.5.Final/jbossws-cxf-resources-5.1.5.Final-wildfly1000",
                artifactPath);
    }

    @Test
    public void testResolveArtifact() throws IOException {
        //GIVEN
        File dirFile = TempFileManager.INSTANCE.newTempDirectory("gradle", null);
        Path gradleCachePath = dirFile.toPath();
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
        File dirFile = TempFileManager.INSTANCE.newTempDirectory("gradle", null);
        Path gradleCachePath = dirFile.toPath();
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
        File dirFile = TempFileManager.INSTANCE.newTempDirectory("gradle", null);
        Path gradleCachePath = dirFile.toPath();
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
