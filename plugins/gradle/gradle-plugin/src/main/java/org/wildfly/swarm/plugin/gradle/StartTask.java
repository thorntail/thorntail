package org.wildfly.swarm.plugin.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;
import org.gradle.initialization.BuildCancellationToken;
import org.slf4j.helpers.MessageFormatter;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.fractions.FractionDescriptor;
import org.wildfly.swarm.fractions.FractionList;
import org.wildfly.swarm.fractions.FractionUsageAnalyzer;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.DependencyManager;
import org.wildfly.swarm.tools.exec.SwarmExecutor;
import org.wildfly.swarm.tools.exec.SwarmProcess;

public class StartTask extends DefaultTask {

    private static final String EXCLUDE_PREFIX = "!";
    private static final String INDENTATION_DELIMITER = "\n  ";
    private static final String DEBUG_PORT_PROPERTY = "thorntail.debug.port";
    private static final String FAILED_TO_START_MESSAGE = "Thorntail process failed to start: ";

    private ThorntailExtension extension;
    private boolean waitForProcess = false;

    public StartTask() {
        extension = getProject().getExtensions().getByType(ThorntailExtension.class);
    }

    void waitForProcess() {
        waitForProcess = true;
    }

    @TaskAction
    public void startApplication() {
        final SwarmExecutor executor;
        if (extension.isUseUberJar()) {
            executor = uberJarExecutor();
        } else if (!getProject().getTasks().withType(War.class).isEmpty()) {
            executor = warExecutor();
        } else if (!getProject().getTasks().withType(Jar.class).isEmpty()) {
            executor = jarExecutor();
        } else {
            throw new GradleException("Unable to determine type of the project, neither war or jar tasks were found.");
        }

        executor.withJVMArguments(extension.getJvmArguments());
        executor.withArguments(extension.getArguments());

        final SwarmProcess process;
        try {

            File processFile;
            try {
                processFile = Files.createTempFile("thorntail-process-file", null).toFile();
                getLogger().info("Thorntail process file: " + processFile.toPath().toString());
            } catch (IOException e) {
                throw new GradleException("Error while creating Thorntail process file");
            }

            process = executor.withDebug(getDebugPort())
                    .withProcessFile(processFile)
                    .withProperties(extension.getProperties())
                    .withProperties(ThorntailUtils.getPropertiesFromFile(extension.getPropertiesFile()))
                    .withStdoutFile(extension.getStdoutFile() != null ? getProject().file(extension.getStdoutFile()).toPath() : null)
                    .withStderrFile(extension.getStderrFile() != null ? getProject().file(extension.getStderrFile()).toPath() : null)
                    .withEnvironment(extension.getEnvironment())
                    .withEnvironment(ThorntailUtils.getPropertiesFromFile(extension.getEnvironmentFile()))
                    .withWorkingDirectory(getProject().getProjectDir().toPath())
                    .withProperty("remote.maven.repo",
                            getProject().getRepositories().withType(MavenArtifactRepository.class).stream()
                                    .map(mavenArtifactRepository -> mavenArtifactRepository.getUrl().toASCIIString())
                                    .collect(Collectors.joining(",")))
                    .execute();
            registerCancellationListeners(process);

            process.awaitReadiness(extension.getStartTimeout(), TimeUnit.SECONDS);

            if (!process.isAlive()) {
                throw new GradleException(
                        MessageFormatter.format(FAILED_TO_START_MESSAGE + "process not ready in {} seconds",
                                extension.getStartTimeout()).getMessage());
            }
            if (process.getError() != null) {
                throw new GradleException(FAILED_TO_START_MESSAGE
                        + (process.getError() != null ? process.getError().getMessage() : "unknown reason"),
                        process.getError());
            }

        } catch (IOException e) {
            throw new GradleException(FAILED_TO_START_MESSAGE + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new GradleException(FAILED_TO_START_MESSAGE + "interrupted", e);
        }

        if (waitForProcess) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                try {
                    process.stop(extension.getStopTimeout(), TimeUnit.SECONDS);
                } catch (InterruptedException ie) {
                    // Do nothing
                }
            } finally {
                process.destroyForcibly();
            }
        } else {
            // save the process into a property so it can be stopped by another task later
            getProject().getExtensions().getExtraProperties().set(PackagePlugin.THORNTAIL_PROCESS_PROPERTY, process);
        }
    }

    private SwarmExecutor uberJarExecutor() {
        getLogger().info("Creating uber-jar Thorntail executor.");
        return new SwarmExecutor().withExecutableJar(getUberJarArchivePath().toPath());
    }

    private SwarmExecutor warExecutor() {
        getLogger().info("Creating WAR Thorntail executor.");

        final String finalName = ThorntailUtils.getArchiveTask(getProject()).getArchiveName();

        final SwarmExecutor executor = new SwarmExecutor()
                .withModules(getModules())
                .withProperty(BootstrapProperties.APP_NAME, finalName)
                .withClassPathEntries(getClassPathEntries(Collections.singletonList(getArchivePath().toPath()), false));

        if (extension.getMainClassName() != null) {
            getLogger().info("With Thorntail app main class " + extension.getMainClassName());
            executor.withMainClass(extension.getMainClassName());
        } else {
            getLogger().info("With default Thorntail app main class.");
            executor.withDefaultMainClass();
        }

        executor.withProperty(BootstrapProperties.APP_PATH, getArchivePath().getPath());
        return executor;
    }

    private SwarmExecutor jarExecutor() {
        getLogger().info("Creating WAR Thorntail executor.");

        final String finalName = ThorntailUtils.getArchiveTask(getProject()).getArchiveName();

        // TODO: application path could possibly contain more classesDirs
        // TODO: getClassesDir() no longer exists in gradle 4+?, gradle-plugins dep should be upgraded, but current
        // version is not available as a maven artifact
//        getProject().getConvention().getPlugin(JavaPluginConvention.class).getSourceSets()
//                .getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput().getClassesDir();
        final List<Path> sourcePaths = Arrays.asList(getProject().getBuildDir().toPath().resolve("classes/java/main"),
                getProject().getBuildDir().toPath().resolve("resources/main"));
        getLogger().info("Source paths: {}", sourcePaths);

        final SwarmExecutor executor = new SwarmExecutor()
                .withModules(getModules())
                .withProperty(BootstrapProperties.APP_NAME, finalName)
                .withClassPathEntries(sourcePaths)
                .withClassPathEntries(getClassPathEntries(sourcePaths, true));

        if (extension.getMainClassName() != null) {
            getLogger().info("With Thorntail app main class " + extension.getMainClassName());
            executor.withMainClass(extension.getMainClassName());
        } else {
            getLogger().info("With default Thorntail app main class.");
            executor.withDefaultMainClass();
        }

        return executor;
    }

    private File getArchivePath() {
        final Jar task = ThorntailUtils.getArchiveTask(getProject());
        final File archivePath = task.getArchivePath();
        getLogger().info("Archive path: {}", archivePath.getAbsolutePath());
        return archivePath;
    }

    private File getUberJarArchivePath() {
        final File outputFile = getProject().getTasks().withType(PackageTask.class)
                .getByName(PackagePlugin.THORNTAIL_PACKAGE_TASK_NAME).getOutputFile();
        getLogger().info("Thorntail Uber-JAR archive path: {}", outputFile.getAbsolutePath());
        return outputFile;
    }

    private List<Path> getModules() {
        List<Path> modules = extension.getModules().stream().map(File::toPath).collect(Collectors.toList());
        getLogger().info("With Thorntail modules:\n  {}",
                modules.stream().map(Path::toString).collect(Collectors.joining(INDENTATION_DELIMITER)));
        return modules;
    }

    private List<Path> getClassPathEntries(List<Path> sourcePaths, boolean scanDependencies) {
        Collection<ArtifactSpec> allDependencies = GradleToolingHelper
                .toDeclaredDependencies(extension.getDependencies()).getRuntimeExplicitAndTransientDependencies();
        getLogger().info("Original dependencies including transitive:\n  {}",
                allDependencies.stream().map(ArtifactSpec::toString).collect(Collectors.joining(INDENTATION_DELIMITER)));

        final List<Path> classpathEntries = allDependencies.stream()
                .map(artifactSpec -> artifactSpec.file.toPath())
                .collect(Collectors.toList());

        final boolean hasThorntailDeps = allDependencies.stream()
                .anyMatch(a -> FractionDescriptor.THORNTAIL_GROUP_ID.equals(a.groupId())
                        && DependencyManager.WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID.equals(a.artifactId()));
        getLogger().info("Thorntail dependencies detected: {}", hasThorntailDeps);

        // automatic fraction detection
        if (extension.getFractionDetectionMode() != BuildTool.FractionDetectionMode.never
                && (extension.getFractionDetectionMode() == BuildTool.FractionDetectionMode.force
                || !hasThorntailDeps)) {
            getLogger().info("Detecting fractions.");

            final FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer(FractionList.get());
            // always scan application source files
            sourcePaths.forEach(analyzer::source);
            // scan dependencies if indicated
            if (scanDependencies) {
                classpathEntries.forEach(analyzer::source);
            }

            final Set<FractionDescriptor> detectedFractions;
            try {
                detectedFractions = analyzer.detectNeededFractions()
                        .stream()
                        // filter out fractions already on classpath
                        .filter(fd -> !allDependencies.contains(ArtifactSpec.fromFractionDescriptor(fd)))
                        .collect(Collectors.toSet());
                getLogger().info("Detected fractions:\n  {}",
                        detectedFractions.stream().map(FractionDescriptor::gav)
                                .collect(Collectors.joining(INDENTATION_DELIMITER)));
            } catch (IOException e) {
                throw new GradleException("Failed to scan for fractions.", e);
            }

            // exclude or add further fractions according to user config
            extension.getFractions().forEach(f -> {
                if (f.startsWith(EXCLUDE_PREFIX)) {
                    FractionDescriptor toRemove = FractionDescriptor.fromGav(FractionList.get(), f.substring(1));
                    getLogger().info("Excluding from detected fractions: {}", toRemove);
                    detectedFractions.remove(toRemove);
                } else {
                    FractionDescriptor toAdd = FractionDescriptor.fromGav(FractionList.get(), f);
                    getLogger().info("Adding to detected fractions: {}", toAdd);
                    detectedFractions.add(toAdd);
                }
            });

            // combine detected fractions with their dependent fractions
            final Set<FractionDescriptor> fractionsWithDependencies = new HashSet<>(detectedFractions);
            fractionsWithDependencies.addAll(detectedFractions.stream()
                    .flatMap(f -> f.getDependencies().stream())
                    .collect(Collectors.toSet()));

            getLogger().info("Detected fractions including dependencies:\n  {}",
                    fractionsWithDependencies.stream()
                            .map(FractionDescriptor::gav)
                            .sorted()
                            .collect(Collectors.joining(INDENTATION_DELIMITER)));

            final Set<ArtifactSpec> fractionsSpecs = fractionsWithDependencies.stream()
                    .map(ArtifactSpec::fromFractionDescriptor)
                    .collect(Collectors.toSet());

            // resolve fraction artifacts and include into classpath entries
            try {
                List<Path> resolvedPaths = artifactResolvingHelper().resolveAll(fractionsSpecs).stream()
                        .map(s -> s.file.toPath())
                        .collect(Collectors.toList());
                classpathEntries.addAll(resolvedPaths);
            } catch (Exception e) {
                throw new GradleException("Failed to resolve fraction dependencies.", e);
            }
        }

        getLogger().info("Computed classpath entries:\n  {}",
                classpathEntries.stream().map(Path::toString).collect(Collectors.joining(INDENTATION_DELIMITER)));

        return classpathEntries;
    }

    private ArtifactResolvingHelper artifactResolvingHelper() {
        return new GradleArtifactResolvingHelper(getProject());
    }

    private void stopProcess(SwarmProcess process) {
        if (process != null && process.isAlive()) {
            try {
                process.stop(extension.getStopTimeout(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            } finally {
                process.destroyForcibly();
            }
        }
    }

    private void registerCancellationListeners(SwarmProcess process) {
        // stop the thorntail process when the build is cancelled, e.g. via CTRL+C
        BuildCancellationToken cancellationToken = getServices().get(BuildCancellationToken.class);
        cancellationToken.addCallback(new Runnable() {
            @Override
            public void run() {
                getLogger().info("Build cancelled, stopping the Thorntail process.");
                stopProcess(process);
                cancellationToken.removeCallback(this);
            }
        });

        // stop the process when the build finishes - this is mainly for the case when the build fails and something
        // prevents the stopTask from stopping the process
        getProject().getGradle().addBuildListener(new BuildListener() {
            @Override
            public void buildStarted(Gradle gradle) {
            }

            @Override
            public void settingsEvaluated(Settings settings) {
            }

            @Override
            public void projectsLoaded(Gradle gradle) {
            }

            @Override
            public void projectsEvaluated(Gradle gradle) {
            }

            @Override
            public void buildFinished(BuildResult result) {
                if (process != null && process.isAlive()) {
                    getLogger().info("Build finished, but the Thorntail process is still active. Stopping it now.");
                    stopProcess(process);
                }
            }
        });
    }

    private Integer getDebugPort() {
        Integer port = null;

        // project property has the highest priority
        final Object value = getProject().findProperty(DEBUG_PORT_PROPERTY);
        if (value != null) {
            try {
                port = Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException e) {
                getLogger().error("Invalid number format for debug port: {}", value);
            }
        }

        // then goes extension attribute
        if (port == null) {
            port = extension.getDebugPort();
        }

        if (port != null) {
            getLogger().info("Waiting on debug port {}.", port);
        }

        return port;
    }
}
