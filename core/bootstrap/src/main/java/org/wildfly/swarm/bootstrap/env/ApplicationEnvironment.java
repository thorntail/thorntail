package org.wildfly.swarm.bootstrap.env;

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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.yaml.snakeyaml.Yaml;

/** Entry-point to runtime environment.
 *
 * <p>This class uses the <code>fraction-manifest.yaml</code> from each fraction,
 * along with the container-wide <code>wildfly-swarm-manifest.yaml</code> if executing
 * in an uberjar mode in order to determine information such as:</p>
 *
 * <ul>
 *     <li>uberjar vs non-uberjar execution mode</li>
 *     <li>removable dependencies</li>
 *     <li>bootstrap swarm artifacts</li>
 *     <li>bootstrap swarm modules</li>
 *     <li>all installed fractions</li>
 * </ul>
 *
 * @author Bob McWhirter
 */
public class ApplicationEnvironment {

    /** Fetch the ApplicationEnvironment
     *
     * @return The environment.
     */
    public static ApplicationEnvironment get() {
        return INSTANCE.updateAndGet((env) -> {
            if (env != null) {
                return env;
            }

            return new ApplicationEnvironment();
        });
    }

    /** Do not construct directly.
     */
    private ApplicationEnvironment() {
        try {
            if (System.getProperty(BootstrapProperties.IS_UBERJAR ) != null) {
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

        if(cpInfoProp!=null) {
            try {

                DependencyTree<MavenArtifactDescriptor> dependencyTree = new DependencyTree<>();

                Yaml yaml = new Yaml();
                Map<String, Object> data = (Map) yaml.load(new FileInputStream(cpInfoProp));
                for (String directDep : data.keySet()) {
                    MavenArtifactDescriptor parent = MavenArtifactDescriptor.fromMavenGav(directDep);
                    Collection<String> transientDeps = (Collection<String>) data.get(directDep);
                    for(String transientDep : transientDeps) {
                        dependencyTree.add(
                                parent,
                                MavenArtifactDescriptor.fromMavenGav(transientDep)
                        );
                    }

                    if(transientDeps.isEmpty())
                        dependencyTree.add(parent);
                }
                this.dependencyTree = Optional.of(dependencyTree);

            } catch (Exception e) {
                throw new RuntimeException("Failed to load cp info", e);
            }
        }
    }

    /** List of bootstrap modules to bootstrap the fractions.
     *
     * @return The list of simple module names
     */
    public List<String> bootstrapModules() {
        return this.bootstrapModules;
    }

    /** List of bootstrap artifacts to look for fractions.
     *
     * @return The list of Maven GAVs to bootstrap.
     */
    public List<String> bootstrapArtifacts() {
        return this.bootstrapArtifacts;
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

        this.bootstrapModules
                .forEach(moduleName -> {
                    try {
                        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(moduleName));
                        ClassLoader cl = module.getClassLoader();

                        Enumeration<URL> results = cl.getResources(FractionManifest.CLASSPATH_LOCATION);

                        while (results.hasMoreElements()) {
                            URL each = results.nextElement();
                            FractionManifest manifest = new FractionManifest(each);
                            this.manifests.add(manifest);
                        }
                    } catch (ModuleLoadException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        this.manifests.sort( new ManifestComparator() );
    }

    private void loadFractionManifests(ClassLoader cl) throws IOException {
        if (this.manifests != null) {
            return;
        }

        this.manifests = new ArrayList<>();

        Enumeration<URL> results = cl.getResources(FractionManifest.CLASSPATH_LOCATION);

        while (results.hasMoreElements()) {
            URL each = results.nextElement();
            FractionManifest manifest = new FractionManifest(each);
            this.manifests.add(manifest);
            if (manifest.getModule() != null) {
                this.bootstrapModules.add(manifest.getModule());
            }
            if (this.mode == Mode.CLASSPATH) {

                Set<MavenArtifactDescriptor> applicationDependencies = new HashSet<>();

                if(dependencyTree.isPresent()) {

                    Set<MavenArtifactDescriptor> topLevelFractions = new HashSet<>();

                    MavenArtifactDescriptor source = new MavenArtifactDescriptor(
                            manifest.getGroupId(),
                            manifest.getArtifactId(),
                            manifest.getVersion()
                    );

                    for (MavenArtifactDescriptor target : dependencyTree.get().getDirectDeps()) {
                        if(source.equals(target))  {
                            topLevelFractions.add(target);
                        }
                    }

                    Set<MavenArtifactDescriptor> keep = new HashSet<>(dependencyTree.get().getDirectDeps());
                    keep.removeAll(topLevelFractions);

                    for(MavenArtifactDescriptor dep : keep) {
                        // the dep itself
                        applicationDependencies.add(dep);

                        // it's transient dependencies
                        applicationDependencies.addAll(dependencyTree.get().getTransientDeps(dep));
                    }


                }

                this.removeableDependencies.addAll(manifest.getDependencies());
                this.removeableDependencies.removeAll(applicationDependencies);

            }
        }

        // match existing dependency info (if given)
        // for now we simply keep all explicit deps, but ignore their transient children
        Set<String> keep = new HashSet<>();
        if(dependencyTree.isPresent()) {

            for(String toBeRemoved : removeableDependencies) {
                MavenArtifactDescriptor source = MavenArtifactDescriptor.fromMavenGav(toBeRemoved);
                for (MavenArtifactDescriptor target : dependencyTree.get().getDirectDeps()) {

                    if (source.groupId().equals(target.groupId()) &&
                            source.artifactId().equals(target.artifactId())
                            ) {
                        keep.add(toBeRemoved);
                    }
                }
            }

            removeableDependencies.removeAll(keep);

        }


        this.manifests.sort( new ManifestComparator() );
    }

    /** List of <i>application-level</i> dependencies.
     *
     * <p>Only applicable for uberjar executions.</p>
     *
     * @return The list of Maven GAVs for application dependencies.
     */
    public List<String> getDependencies() {
        if (this.mode == Mode.UBERJAR) {
            return this.applicationManifest.getDependencies();
        }
        return Collections.emptyList();
    }

    /** List of <i>removable</i> dependencies, such as the
     *  bootstrap Swarm jars and anything transitive not directly
     *  required by the application.
     *
     * @return The list of Maven GAVs that may be removed.
     */
    public List<String> getRemovableDependencies() {
        return this.removeableDependencies;
    }

    /**
     *
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
     *
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

    /** Retrieve the user's main-class name, if specified, else the default.
     *
     * @return The user's main-class name, else the default Swarm main.
     */
    public String getMainClassName() {
        if (this.mode == Mode.UBERJAR) {
            return this.applicationManifest.getMainClass();
        }

        return DEFAULT_MAIN_CLASS_NAME;
    }

    /** Determine if this application is defined to be hollow.
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
                return Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.bootstrap")).getClassLoader();
            } catch (ModuleLoadException e) {
                // ignore
            }
        }
        return ApplicationEnvironment.class.getClassLoader();
    }

    /** Retrieve a sorted list of installed fraction manifests.
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
