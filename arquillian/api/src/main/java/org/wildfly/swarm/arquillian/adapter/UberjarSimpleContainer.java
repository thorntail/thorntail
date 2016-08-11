/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.arquillian.adapter;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.arquillian.daemon.DaemonServiceActivator;
import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.msc.ServiceActivatorArchive;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.internal.SwarmInternalProperties;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.exec.SwarmExecutor;
import org.wildfly.swarm.tools.exec.SwarmProcess;

public class UberjarSimpleContainer implements SimpleContainer {

    private final ContainerContext containerContext;

    public UberjarSimpleContainer(ContainerContext containerContext, Class<?> testClass) {
        this.containerContext = containerContext;
        this.testClass = testClass;
    }

    @Override
    public UberjarSimpleContainer requestedMavenArtifacts(Set<String> artifacts) {
        this.requestedMavenArtifacts = artifacts;

        return this;
    }

    public UberjarSimpleContainer setJavaVmArguments(String javaVmArguments) {
        this.javaVmArguments = javaVmArguments;
        return this;
    }

    @Override
    public void start(Archive<?> archive) throws Exception {
  /*
        System.err.println( ">>> CORE" );
        System.err.println(" NAME: " + archive.getName());
        for (Map.Entry<ArchivePath, Node> each : archive.getContent().entrySet()) {
            System.err.println("-> " + each.getKey());
        }
        System.err.println( "<<< CORE" );
        */

        //System.err.println("is factory: " + isContainerFactory(this.testClass));

        MainSpecifier mainSpecifier = containerContext.getObjectStore().get(MainSpecifier.class);

        boolean annotatedCreateSwarm = false;

        Method swarmMethod = getAnnotatedMethodWithAnnotation(this.testClass, CreateSwarm.class);
        // preflight check it
        if (swarmMethod != null) {
            if (Modifier.isStatic(swarmMethod.getModifiers())) {
                // good to go
                annotatedCreateSwarm = true;
                archive.as(JARArchive.class)
                        .addClass(CreateSwarm.class)
                        .addClass(AnnotationBasedMain.class)
                        .addClass(this.testClass);
                archive.as(JARArchive.class).addModule("org.wildfly.swarm.container");
                archive.as(JARArchive.class).addModule("org.wildfly.swarm.configuration");
            } else {
                throw new IllegalArgumentException(
                        String.format("Method annotated with %s is %s but it is not static",
                                      CreateSwarm.class.getSimpleName(),
                                      swarmMethod));
            }
        } else {
            //TODO Some kind of default main()?
        }

        archive.as(ServiceActivatorArchive.class)
                .addServiceActivator(DaemonServiceActivator.class);
        archive.as(JARArchive.class).addModule("org.wildfly.swarm.arquillian.daemon");
        archive.as(JARArchive.class).addModule("org.jboss.modules");
        archive.as(JARArchive.class).addModule("org.jboss.msc");

        BuildTool tool = new BuildTool()
                .projectArchive(archive)
                .fractionDetectionMode(BuildTool.FractionDetectionMode.never)
                .bundleDependencies(false);

        final String additionalModules = System.getProperty(SwarmInternalProperties.BUILD_MODULES);
        if (additionalModules != null) {
            tool.additionalModules(Stream.of(additionalModules.split(":"))
                                           .map(File::new)
                                           .filter(File::exists)
                                           .map(File::getAbsolutePath)
                                           .collect(Collectors.toList()));
        }

        final SwarmExecutor executor = new SwarmExecutor().withDefaultSystemProperties();

        if (annotatedCreateSwarm) {
            executor.withProperty(AnnotationBasedMain.ANNOTATED_CLASS_NAME, this.testClass.getName());
        }

        final String additionalRepos = System.getProperty(SwarmInternalProperties.BUILD_REPOS);
        if (additionalRepos != null) {
            executor.withProperty("remote.maven.repo", additionalRepos);
        }

        final ShrinkwrapArtifactResolvingHelper resolvingHelper = ShrinkwrapArtifactResolvingHelper.defaultInstance();
        tool.artifactResolvingHelper(resolvingHelper);

        boolean hasRequestedArtifacts = this.requestedMavenArtifacts != null && this.requestedMavenArtifacts.size() > 0;

        if (!hasRequestedArtifacts) {
            final MavenResolvedArtifact[] deps =
                    resolvingHelper.withResolver(r -> r.loadPomFromFile("pom.xml")
                            .importRuntimeAndTestDependencies()
                            .resolve()
                            .withTransitivity()
                            .asResolvedArtifact());

            for (MavenResolvedArtifact dep : deps) {
                MavenCoordinate coord = dep.getCoordinate();
                tool.dependency(dep.getScope().name(), coord.getGroupId(),
                                coord.getArtifactId(), coord.getVersion(),
                                coord.getPackaging().getExtension(), coord.getClassifier(), dep.asFile());
            }
        } else {
            // ensure that arq daemon is available
            this.requestedMavenArtifacts.add("org.wildfly.swarm:arquillian-daemon");
            for (String requestedDep : this.requestedMavenArtifacts) {
                final MavenResolvedArtifact[] deps =
                        resolvingHelper.withResolver(r -> r.loadPomFromFile("pom.xml")
                                .resolve(requestedDep)
                                .withTransitivity()
                                .asResolvedArtifact());

                for (MavenResolvedArtifact dep : deps) {
                    MavenCoordinate coord = dep.getCoordinate();
                    tool.dependency(dep.getScope().name(), coord.getGroupId(),
                                    coord.getArtifactId(), coord.getVersion(),
                                    coord.getPackaging().getExtension(), coord.getClassifier(), dep.asFile());
                }
            }
        }

        final String debug = System.getProperty(SwarmProperties.DEBUG_PORT);
        if (debug != null) {
            try {
                executor.withDebug(Integer.parseInt(debug));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Failed to parse %s of \"%s\"", SwarmProperties.DEBUG_PORT, debug),
                                                   e);
            }
        }

        if (mainSpecifier != null) {
            tool.mainClass(mainSpecifier.getClassName());
            String[] args = mainSpecifier.getArgs();

            for (String arg : args) {
                executor.withArgument(arg);
            }
        } else if (annotatedCreateSwarm){
            tool.mainClass(AnnotationBasedMain.class.getName());
        }

        Archive<?> wrapped = null;
        try {
            wrapped = tool.build();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }

        if (BootstrapProperties.flagIsSet(SwarmInternalProperties.EXPORT_UBERJAR)) {
            final File out = new File(wrapped.getName());
            System.err.println("Exporting swarm jar to " + out.getAbsolutePath());
            wrapped.as(ZipExporter.class).exportTo(out, true);
        }

        /* for (Map.Entry<ArchivePath, Node> each : wrapped.getContent().entrySet()) {
                System.err.println("-> " + each.getKey());
            }*/

        File executable = File.createTempFile("arquillian", "-swarm.jar");
        wrapped.as(ZipExporter.class).exportTo(executable, true);
        executable.deleteOnExit();

        executor.withProperty("java.net.preferIPv4Stack", "true");
        executor.withJVMArguments(getJavaVmArgumentsList());
        executor.withExecutableJar(executable.toPath());

        File workingDirectory = Files.createTempDirectory("arquillian").toFile();
        workingDirectory.deleteOnExit();
        executor.withWorkingDirectory(workingDirectory.toPath());

        this.process = executor.execute();
        this.process.getOutputStream().close();

        this.process.awaitDeploy(2, TimeUnit.MINUTES);

        if (!this.process.isAlive()) {
            throw new DeploymentException("Process failed to start");
        }
        if (this.process.getError() != null) {
            throw new DeploymentException("Error starting process", this.process.getError());
        }
    }

    private void registerContainerFactory(Archive<?> archive, Class<?> clazz) {
        archive.as(JavaArchive.class)
                .addAsServiceProvider("org.wildfly.swarm.ContainerFactory",
                                      clazz.getName())
                .addClass(clazz);
        archive.as(JARArchive.class).addModule("org.wildfly.swarm.container");
        archive.as(JARArchive.class).addModule("org.wildfly.swarm.configuration");
    }

    @Override
    public void stop() throws Exception {
        this.process.stop();
    }

    private String ga(final MavenCoordinate coord) {
        return String.format("%s:%s", coord.getGroupId(), coord.getArtifactId());
    }

    private String gav(final MavenCoordinate coord) {
        return gav(coord.getGroupId(), coord.getArtifactId(), coord.getVersion());
    }

    private String gav(final String group, final String artifact, final String version) {
        return String.format("%s:%s:%s", group, artifact, version);
    }

    private List<String> getJavaVmArgumentsList() {
        if (this.javaVmArguments == null) {
            return Collections.emptyList();
        }

        List<String> args = new ArrayList<>();

        StringTokenizer tokens = new StringTokenizer(this.javaVmArguments);

        while (tokens.hasMoreTokens()) {
            args.add(tokens.nextToken());
        }

        return args;

    }

    private final Class<?> testClass;

    private SwarmProcess process;

    private Set<String> requestedMavenArtifacts;

    private String javaVmArguments;

}

