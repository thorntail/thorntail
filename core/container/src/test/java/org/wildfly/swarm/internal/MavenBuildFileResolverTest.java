package org.wildfly.swarm.internal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.fest.assertions.Assertions.assertThat;

public class MavenBuildFileResolverTest {

    @Rule
    public final EnvironmentVariables environmentVariables  = new EnvironmentVariables();

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void shouldResolvePomInRootOfTheProject() throws IOException {
        // given
        environmentVariables.set("MAVEN_CMD_LINE_ARGS", "clean install");
        final File projectFolder = tmpFolder.newFolder("maven-project");
        final File expectedPomFile = tmpFolder.newFile("maven-project/pom.xml");

        // when
        final Path path = MavenBuildFileResolver.resolveMavenBuildFileName(projectFolder.getAbsolutePath());

        // then
        assertThat(path.toFile()).isEqualTo(expectedPomFile);
    }

    @Test
    public void shouldResolvePomInRootOfTheProjectWhenSpecifiedUsingFileFlag() throws IOException {
        // given
        environmentVariables.set("MAVEN_CMD_LINE_ARGS", "clean install -f pom.xml");
        final File projectFolder = tmpFolder.newFolder("maven-project");
        final File expectedPomFile = tmpFolder.newFile("maven-project/pom.xml");

        // when
        final Path path = MavenBuildFileResolver.resolveMavenBuildFileName(projectFolder.getAbsolutePath());

        // then
        assertThat(path.toFile()).isEqualTo(expectedPomFile);
    }

    @Test
    public void shouldResolvePomInRootOfTheProjectWhenSpecifiedUsingFileFlagWithCurrentFolder() throws IOException {
        // given
        environmentVariables.set("MAVEN_CMD_LINE_ARGS", "clean install -f ./pom.xml");
        final File projectFolder = tmpFolder.newFolder("maven-project");
        final File expectedPomFile = tmpFolder.newFile("maven-project/pom.xml");

        // when
        final Path path = MavenBuildFileResolver.resolveMavenBuildFileName(projectFolder.getAbsolutePath());

        // then
        assertThat(path.toFile()).isEqualTo(expectedPomFile);
    }

    @Test
    public void shouldResolvePomFileInSiblingFolderWhenFileFlagSpecifiedWithRelativePath() throws IOException {
        environmentVariables.set("MAVEN_CMD_LINE_ARGS", "clean install -X -f ../bootstrap/pom.xml");
        final File bootstrapModule = tmpFolder.newFolder("maven-project", "bootstrap");
        final File coreModule = tmpFolder.newFolder("maven-project", "core");
        final File expectedPomFile = tmpFolder.newFile("maven-project/bootstrap/pom.xml");

        // when
        final Path path = MavenBuildFileResolver.resolveMavenBuildFileName(coreModule.getAbsolutePath());

        // then
        assertThat(path.toFile()).isEqualTo(expectedPomFile);
    }

    @Test
    public void shouldResolvePomFileSpecifiedUsingAbsoluteFolderPath() throws IOException {
        final File bootstrapModule = tmpFolder.newFolder("maven-project", "bootstrap");
        final File expectedPomFile = tmpFolder.newFile("maven-project/bootstrap/pom.xml");
        environmentVariables.set("MAVEN_CMD_LINE_ARGS", "clean install -X -f " + bootstrapModule.getAbsolutePath());

        // when
        final Path path = MavenBuildFileResolver.resolveMavenBuildFileName(System.getProperty("user.dir"));

        // then
        assertThat(path.toFile()).isEqualTo(expectedPomFile);
    }

    @Test
    public void shouldResolvePomFileSpecifiedUsingAbsoluteFilePath() throws IOException {
        final File bootstrapModule = tmpFolder.newFolder("maven-project", "bootstrap");
        final File expectedPomFile = tmpFolder.newFile("maven-project/bootstrap/pom.xml");
        environmentVariables.set("MAVEN_CMD_LINE_ARGS", "clean install -X -f " + bootstrapModule.getAbsolutePath() + File.separator + "pom.xml");

        // when
        final Path path = MavenBuildFileResolver.resolveMavenBuildFileName(System.getProperty("user.dir"));

        // then
        assertThat(path.toFile()).isEqualTo(expectedPomFile);
    }

}