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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ManifestContainer;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.arquillian.adapter.resources.ContextRoot;
import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.spi.api.DependenciesContainer;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.MarkerContainer;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.internal.SwarmInternalProperties;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.DeclaredDependencies;
import org.wildfly.swarm.tools.exec.SwarmExecutor;
import org.wildfly.swarm.tools.exec.SwarmProcess;


public class UberjarSimpleContainer implements SimpleContainer {

    private final ContainerContext containerContext;

    private final DeploymentContext deploymentContext;

    public UberjarSimpleContainer(ContainerContext containerContext, DeploymentContext deploymentContext, Class<?> testClass) {
        this.containerContext = containerContext;
        this.deploymentContext = deploymentContext;
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

        archive.add(EmptyAsset.INSTANCE, "META-INF/arquillian-testable");

        ContextRoot contextRoot = null;
        if (archive.getName().endsWith(".war")) {
            contextRoot = new ContextRoot("/");
            Node jbossWebNode = archive.as(WebArchive.class).get("WEB-INF/jboss-web.xml");
            if (jbossWebNode != null) {
                if (jbossWebNode.getAsset() != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(jbossWebNode.getAsset().openStream()))) {
                        String content = String.join("\n", reader.lines().collect(Collectors.toList()));

                        Pattern pattern = Pattern.compile("<context-root>(.+)</context-root>");
                        Matcher matcher = pattern.matcher(content);
                        if (matcher.find()) {
                            contextRoot = new ContextRoot(matcher.group(1));
                        }
                    }
                }
            }

            this.deploymentContext.getObjectStore().add(ContextRoot.class, contextRoot);
        }


        MainSpecifier mainSpecifier = containerContext.getObjectStore().get(MainSpecifier.class);

        boolean annotatedCreateSwarm = false;

        Method swarmMethod = getAnnotatedMethodWithAnnotation(this.testClass, CreateSwarm.class);

        List<Class<?>> types = determineTypes(this.testClass);

        // preflight check it
        if (swarmMethod != null) {
            if (Modifier.isStatic(swarmMethod.getModifiers())) {
                // good to go
                annotatedCreateSwarm = true;
                types.add(CreateSwarm.class);
                types.add(AnnotationBasedMain.class);

                archive.as(JARArchive.class).addModule("org.wildfly.swarm.container");
                archive.as(JARArchive.class).addModule("org.wildfly.swarm.configuration");
            } else {
                throw new IllegalArgumentException(
                        String.format("Method annotated with %s is %s but it is not static",
                                      CreateSwarm.class.getSimpleName(),
                                      swarmMethod));
            }
        }

        if (types.size() > 0) {
            try {
                ((ClassContainer<?>) archive).addClasses(types.toArray(new Class[types.size()]));
            } catch (UnsupportedOperationException e) {
                // TODO Remove the try/catch when SHRINKWRAP-510 is resolved and we update to latest SW
                archive.as(JARArchive.class).addClasses(types.toArray(new Class[types.size()]));
            }
        }


        final ShrinkwrapArtifactResolvingHelper resolvingHelper = ShrinkwrapArtifactResolvingHelper.defaultInstance();

        BuildTool tool = new BuildTool(resolvingHelper)
                .fractionDetectionMode(BuildTool.FractionDetectionMode.when_missing)
                .bundleDependencies(false);

        String additionalModules = System.getProperty(SwarmInternalProperties.BUILD_MODULES);

        // See https://issues.jboss.org/browse/SWARM-571
        if (null == additionalModules) {
            // see if we can find it
            File modulesDir = new File("target/classes/modules");
            additionalModules = modulesDir.exists() ? modulesDir.getAbsolutePath() : null;
        }

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

        if (contextRoot != null) {
            executor.withProperty(SwarmProperties.CONTEXT_PATH, contextRoot.context());
        }

        executor.withProperty("swarm.inhibit.auto-stop", "true");

        String additionalRepos = System.getProperty(SwarmInternalProperties.BUILD_REPOS);
        if (additionalRepos != null) {
            additionalRepos = additionalRepos + ",";
        } else {
            additionalRepos = "";
        }
        additionalRepos = additionalRepos + "https://repository.jboss.org/nexus/content/groups/public/";
        executor.withProperty("remote.maven.repo", additionalRepos);


        // project dependencies
        FileSystemLayout fsLayout = FileSystemLayout.create();
        DeclaredDependencies declaredDependencies =
                DependencyDeclarationFactory.newInstance(fsLayout).create(fsLayout, resolvingHelper);
        tool.declaredDependencies(declaredDependencies);

        // check for "org.wildfly.swarm.allDependencies" flag
        // see DependenciesContainer#addAllDependencies()
        if (archive instanceof DependenciesContainer) {
            DependenciesContainer<?> depContainer = (DependenciesContainer<?>) archive;
            if (depContainer.hasMarker(DependenciesContainer.ALL_DEPENDENCIES_MARKER)) {
                munge(depContainer, declaredDependencies);
            }
        } else if (archive instanceof WebArchive) {
            // Handle the default deployment of type WAR
            WebArchive webArchive = (WebArchive) archive;
            if (MarkerContainer.hasMarker(webArchive, DependenciesContainer.ALL_DEPENDENCIES_MARKER)) {
                munge(webArchive, declaredDependencies);
            }
        }

        tool.projectArchive(archive);


        final String debug = System.getProperty(SwarmProperties.DEBUG_PORT);
        if (debug != null) {
            try {
                executor.withDebug(Integer.parseInt(debug));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("Failed to parse %s of \"%s\"", SwarmProperties.DEBUG_PORT, debug), e
                );
            }
        }

        if (mainSpecifier != null) {
            tool.mainClass(mainSpecifier.getClassName());
            String[] args = mainSpecifier.getArgs();

            for (String arg : args) {
                executor.withArgument(arg);
            }
        } else if (annotatedCreateSwarm) {
            tool.mainClass(AnnotationBasedMain.class.getName());
        } else {
            Optional<String> mainClassName = Optional.empty();
            Node node = archive.get("META-INF/arquillian-main-class");
            if (node != null && node.getAsset() != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(node.getAsset().openStream()))) {
                    mainClassName = reader.lines().findFirst();
                }
            }
            tool.mainClass(mainClassName.orElse(Swarm.class.getName()));
        }

        if (this.testClass != null) {
            tool.testClass(this.testClass.getName());
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

        executable = File.createTempFile(TempFileManager.WFSWARM_TMP_PREFIX + "arquillian", "-swarm.jar");
        wrapped.as(ZipExporter.class).exportTo(executable, true);

        String mavenRepoLocal = System.getProperty("maven.repo.local");

        if (mavenRepoLocal != null) {
            executor.withProperty("maven.repo.local", mavenRepoLocal);
        }

        executor.withProperty("java.net.preferIPv4Stack", "true");

        File processFile = File.createTempFile(TempFileManager.WFSWARM_TMP_PREFIX + "mainprocessfile", null);

        executor.withProcessFile(processFile);

        executor.withJVMArguments(getJavaVmArgumentsList());
        executor.withExecutableJar(executable.toPath());

        workingDirectory = TempFileManager.INSTANCE.newTempDirectory("arquillian", null);
        executor.withWorkingDirectory(workingDirectory.toPath());

        this.process = executor.execute();
        this.process.getOutputStream().close();

        this.process.awaitReadiness(2, TimeUnit.MINUTES);

        if (!this.process.isAlive()) {
            throw new DeploymentException("Process failed to start");
        }
        if (this.process.getError() != null) {
            throw new DeploymentException("Error starting process", this.process.getError());
        }
    }

    private <C extends LibraryContainer<?> & ManifestContainer<?>> void munge(C container, DeclaredDependencies declaredDependencies) {

        for (ArtifactSpec artifact : declaredDependencies.getRuntimeExplicitAndTransientDependencies()) {
            assert artifact.file != null : "artifact.file cannot be null at this point: " + artifact;
            container.addAsLibrary(artifact.file);
        }
        try {
            MarkerContainer.addMarker(container, "org.wildfly.swarm.allDependencies.added");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Class<?>> determineTypes(Class<?> testClass) {
        List<Class<?>> types = new ArrayList<>();

        Class<?> clazz = testClass;

        while (clazz != null && clazz != Object.class) {
            types.add(clazz);
            List<Class<?>> interfaces = Arrays.<Class<?>>asList(clazz.getInterfaces());
            types.addAll(interfaces);
            // Handle interfaces with inheritance
            for (Class<?> iface : interfaces) {
                List<Class<?>> superInterfaces = Arrays.<Class<?>>asList(iface.getInterfaces());
                types.addAll(superInterfaces);
            }

            clazz = clazz.getSuperclass();
        }

        return types;
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
        TempFileManager.deleteRecursively(workingDirectory);
        executable.delete();
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

    private void deleteRecursively(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }

        file.delete();
    }

    private final Class<?> testClass;

    private SwarmProcess process;

    private Set<String> requestedMavenArtifacts = new HashSet<>();

    private String javaVmArguments;

    private File workingDirectory;

    private File executable;

}

