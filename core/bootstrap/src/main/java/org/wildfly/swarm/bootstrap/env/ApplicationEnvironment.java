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
package org.wildfly.swarm.bootstrap.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.maven.ArtifactCoordinates;
import org.wildfly.swarm.bootstrap.modules.MavenResolvers;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.yaml.snakeyaml.Yaml;

/**
 * Entry-point to runtime environment.
 *
 * <p>This class uses the <code>fraction-manifest.yaml</code> from each fraction,
 * along with the container-wide <code>wildfly-swarm-manifest.yaml</code> if executing
 * in an uberjar mode in order to determine information such as:</p>
 *
 * <ul>
 * <li>uberjar vs non-uberjar execution mode</li>
 * <li>removable dependencies</li>
 * <li>bootstrap swarm artifacts</li>
 * <li>bootstrap swarm modules</li>
 * <li>all installed fractions</li>
 * </ul>
 *
 * @author Bob McWhirter
 */
public class ApplicationEnvironment {

    /**
     * Fetch the ApplicationEnvironment
     *
     * @return The environment.
     */
    public static ApplicationEnvironment get() {
        return INSTANCE.updateAndGet((env) -> {
            if (env != null) {
                return env;
            }

            try (AutoCloseable handle = Performance.time("Load application environment")) {
                return new ApplicationEnvironment();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Do not construct directly.
     */
    private ApplicationEnvironment() {
        try {
            if (System.getProperty(BootstrapProperties.IS_UBERJAR) != null) {
                this.mode = Mode.UBERJAR;
                if (!loadWildFlySwarmApplicationManifestFromClasspath()) {
                    loadWildFlySwarmApplicationManifestFromTCCL();
                }
            } else {
                this.mode = Mode.CLASSPATH;
                loadDependencyTree();
                loadFractionManifestsFromClasspath();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDependencyTree() {
        final String cpInfoProp = System.getProperty("swarm.cp.info");

        if (cpInfoProp == null) {
            return;
        }

        final DependencyTree<MavenArtifactDescriptor> dependencyTree = new DependencyTree<>();
        final Yaml yaml = new Yaml();

        try (final FileInputStream fileStream = new FileInputStream(cpInfoProp)) {
            @SuppressWarnings("unchecked") final Map<String, Collection<String>> data = yaml.loadAs(fileStream, Map.class);

            for (final Entry<String, Collection<String>> entry : data.entrySet()) {
                final MavenArtifactDescriptor parent = MavenArtifactDescriptor.fromMavenGav(entry.getKey());
                final Collection<String> transientDeps = entry.getValue();
                if (transientDeps != null && !transientDeps.isEmpty()) {
                    for (final String transientDep : transientDeps) {
                        dependencyTree.add(
                                parent,
                                MavenArtifactDescriptor.fromMavenGav(transientDep)
                        );
                    }
                } else {
                    dependencyTree.add(parent);
                }
            }

            this.dependencyTree = Optional.of(dependencyTree);

        } catch (final IOException e) {
            throw new RuntimeException("Failed to load cp info", e);
        }
    }

    /**
     * List of bootstrap modules to bootstrap the fractions.
     *
     * @return The list of simple module names
     */
    public List<String> bootstrapModules() {
        return this.bootstrapModules;
    }

    /**
     * List of bootstrap artifacts to look for fractions.
     *
     * @return The list of Maven GAVs to bootstrap.
     */
    public List<String> bootstrapArtifacts() {
        return this.bootstrapArtifacts;
    }

    public List<ArtifactCoordinates> bootstrapArtifactsAsCoordinates() {
        return this.bootstrapArtifacts.stream().map(artifact -> {
            String[] parts = artifact.split(":");
            ArtifactCoordinates coords = null;
            if (parts.length == 4) {
                coords = new ArtifactCoordinates(parts[0], parts[1], parts[3]);
            } else if (parts.length == 5) {
                coords = new ArtifactCoordinates(parts[0], parts[1], parts[3], parts[4]);
            }
            return coords;
        }).collect(Collectors.toList());
    }

    public Mode getMode() {
        return mode;
    }

    private boolean loadWildFlySwarmApplicationManifestFromClasspath() throws IOException {
        return loadWildFlySwarmApplicationManifest(ClassLoader.getSystemClassLoader());
    }

    private boolean loadWildFlySwarmApplicationManifestFromTCCL() throws IOException {
        return loadWildFlySwarmApplicationManifest(Thread.currentThread().getContextClassLoader());
    }

    private boolean loadWildFlySwarmApplicationManifest(ClassLoader cl) throws IOException {
        URL url = cl.getResource(WildFlySwarmManifest.CLASSPATH_LOCATION);
        if (url == null) {
            return false;
        }
        this.applicationManifest = new WildFlySwarmManifest(url);
        this.bootstrapModules.addAll(this.applicationManifest.bootstrapModules());
        this.bootstrapArtifacts.addAll(this.applicationManifest.bootstrapArtifacts());
        return true;
    }

    private void loadFractionManifestsFromClasspath() throws IOException {
        loadFractionManifests(ClassLoader.getSystemClassLoader());
    }

    private void loadFractionManifestsFromUberjar() throws IOException, ModuleLoadException {
        if (this.manifests != null) {
            return;
        }

        this.manifests = new ArrayList<>();
        Set<String> modulesManifests = new HashSet<>();

        this.bootstrapModules
                .forEach(moduleName -> {
                    try {
                        Module module = Module.getBootModuleLoader().loadModule(moduleName);
                        ClassLoader cl = module.getClassLoader();

                        Enumeration<URL> results = cl.getResources(FractionManifest.CLASSPATH_LOCATION);

                        while (results.hasMoreElements()) {
                            URL each = results.nextElement();
                            FractionManifest manifest = new FractionManifest(each);
                            this.manifests.add(manifest);
                            modulesManifests.add(manifest.getGroupId() + manifest.getArtifactId());
                        }
                    } catch (ModuleLoadException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        bootstrapArtifactsAsCoordinates().forEach((coords) -> {
            try {
                File artifactFile = MavenResolvers.get().resolveJarArtifact(coords);
                if (artifactFile == null) {
                    throw new RuntimeException("Unable to resolve artifact from coordinates: " + coords);
                }
                try (ZipFile zip = new ZipFile(artifactFile)) {
                    ZipEntry manifestEntry = zip.getEntry(FractionManifest.CLASSPATH_LOCATION);
                    if (manifestEntry != null) {
                        FractionManifest manifest = new FractionManifest(zip.getInputStream(manifestEntry));
                        if (!modulesManifests.contains(manifest.getGroupId() + manifest.getArtifactId())) {
                            this.manifests.add(manifest);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadFractionManifests(ClassLoader cl) throws IOException {
        if (this.manifests != null) {
            return;
        }

        this.manifests = new ArrayList<>();

        Enumeration<URL> results = cl.getResources(FractionManifest.CLASSPATH_LOCATION);
        Set<MavenArtifactDescriptor> fractionDependencies = new HashSet<>();

        while (results.hasMoreElements()) {
            URL each = results.nextElement();
            FractionManifest manifest = new FractionManifest(each);
            this.manifests.add(manifest);
            if (manifest.getModule() != null) {
                this.bootstrapModules.add(manifest.getModule());
            }
            if (this.mode == Mode.CLASSPATH) {

                MavenArtifactDescriptor fraction = new MavenArtifactDescriptor(
                        manifest.getGroupId(),
                        manifest.getArtifactId(),
                        manifest.getVersion()
                );

                if (dependencyTree.isPresent()) {

                    for (MavenArtifactDescriptor directDep : dependencyTree.get().getDirectDeps()) {
                        if (fraction.equals(directDep)) {
                            fractionDependencies.add(directDep);
                        }
                    }

                }

                this.removeableDependencies.add(fraction.mavenGav());
                this.removeableDependencies.addAll(manifest.getDependencies());

            }
        }

        if (dependencyTree.isPresent()) {

            Set<String> applicationDependencies = new HashSet<>();
            Set<MavenArtifactDescriptor> keep = new HashSet<>(dependencyTree.get().getDirectDeps());
            keep.removeAll(fractionDependencies);


            for (MavenArtifactDescriptor dep : keep) {
                // the dep itself
                applicationDependencies.add(dep.mavenGav());

                // it's transient dependencies
                applicationDependencies.addAll(dependencyTree.get().getTransientDeps(dep).stream().map(t -> t.mavenGav()).collect(Collectors.toList()));
            }

            this.removeableDependencies.removeAll(applicationDependencies);
        }
    }

    /**
     * List of <i>application-level</i> dependencies.
     *
     * <p>Only applicable for uberjar executions.</p>
     *
     * @return The list of Maven GAVs for application dependencies.
     */
    public Set<String> getDependencies() {
        if (this.mode == Mode.UBERJAR) {
            return this.applicationManifest.getDependencies();
        }
        return Collections.emptySet();
    }

    /**
     * List of <i>removable</i> dependencies, such as the
     * bootstrap Swarm jars and anything transitive not directly
     * required by the application.
     *
     * @return The list of Maven GAVs that may be removed.
     */
    public List<String> getRemovableDependencies() {
        return this.removeableDependencies;
    }

    /**
     * [hb] TODO: these javadocs are wrong and describe a previous implementation of this method
     *
     * Resolve the application's dependencies.
     *
     * <p>Using combinations of {@link #getDependencies()}} and {@link #getRemovableDependencies()},
     * depending on execution mode, resolves application dependencies, taking
     * into account any exclusions.</p>
     *
     * @param exclusions Maven GAVs to exclude.
     * @return Set of paths to dependency artifacts.
     * @throws IOException
     */
    public Set<String> resolveDependencies(List<String> exclusions) throws IOException {
        if (this.mode == Mode.UBERJAR) {
            return new MavenDependencyResolution().resolve(exclusions);
        } else {
            return new SystemDependencyResolution().resolve(exclusions);
        }
    }

    public String getAsset() {
        if (this.mode == Mode.UBERJAR) {
            return this.applicationManifest.getAsset();
        }

        return null;
    }

    /**
     * Retrieve the user's main-class name, if specified, else the default.
     *
     * @return The user's main-class name, else the default Swarm main.
     */
    public String getMainClassName() {
        if (this.mode == Mode.UBERJAR) {
            return this.applicationManifest.getMainClass();
        }

        return DEFAULT_MAIN_CLASS_NAME;
    }

    /**
     * Determine if this application is defined to be hollow.
     *
     * @return <code>true</code> if hollow, otherwise <code>false</code>.
     */
    public boolean isHollow() {
        if (this.mode == Mode.UBERJAR) {
            return this.applicationManifest.isHollow();
        }

        return false;
    }

    private Path root() {
        URL location = ApplicationEnvironment.class.getProtectionDomain().getCodeSource().getLocation();
        if (location.getProtocol().equals("file")) {
            try {
                return Paths.get(location.toURI());
            } catch (URISyntaxException e) {
                return null;
            }
        }

        return null;
    }

    public ClassLoader getBootstrapClassLoader() throws ModuleLoadException {
        if (this.mode == Mode.UBERJAR) {
            try {
                //ClassLoader cl = Module.getBootModuleLoader().loadModule("org.wildfly.swarm.bootstrap").getClassLoader();
                Module module = Module.getBootModuleLoader().loadModule("org.wildfly.swarm.bootstrap");
                ClassLoader cl = module.getClassLoader();
                return cl;
            } catch (ModuleLoadException e) {
                // ignore
            }
        }
        return ApplicationEnvironment.class.getClassLoader();
    }

    /**
     * Retrieve a sorted list of installed fraction manifests.
     *
     * @return The sorted list of installed fraction manifests.
     */
    public List<FractionManifest> fractionManifests() {
        if (this.mode == Mode.UBERJAR && this.manifests == null) {
            try {
                loadFractionManifestsFromUberjar();
            } catch (IOException | ModuleLoadException e) {
                throw new RuntimeException(e);
            }
        }
        return this.manifests;
    }

    private Optional<DependencyTree<MavenArtifactDescriptor>> dependencyTree = Optional.empty();

    public enum Mode {
        UBERJAR,
        CLASSPATH
    }

    private Mode mode = Mode.CLASSPATH;

    private List<FractionManifest> manifests;

    private WildFlySwarmManifest applicationManifest;

    private List<String> bootstrapModules = new ArrayList<>();

    private List<String> bootstrapArtifacts = new ArrayList<>();

    private List<String> removeableDependencies = new ArrayList<>();

    private static AtomicReference<ApplicationEnvironment> INSTANCE = new AtomicReference<>();

    public static final String DEFAULT_MAIN_CLASS_NAME = "org.wildfly.swarm.Swarm";

}
