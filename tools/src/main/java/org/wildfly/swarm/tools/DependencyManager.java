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
package org.wildfly.swarm.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmApplicationConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmBootstrapConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmClasspathConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class DependencyManager {

    public static final String WILDFLY_SWARM_GROUP_ID = "org.wildfly.swarm";

    public static final String WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID = "bootstrap";

    public static final String JBOSS_MODULES_GROUP_ID = "org.jboss.modules";

    public static final String JBOSS_MODULES_ARTIFACT_ID = "jboss-modules";

    public DependencyManager() {
    }

    public void setArtifactResolvingHelper(ArtifactResolvingHelper resolver) {
        this.resolver = resolver;
    }

    public void addExplicitDependency(ArtifactSpec dep) {
        this.explicitDependencies.add(dep);
    }

    public void addPresolvedDependency(ArtifactSpec dep) {
        this.presolvedDependencies.add(dep);
    }

    public void addAdditionalModule(Path module) {
        try {
            analyzeModuleDependencies(new ModuleAnalyzer(module));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean includeAsBootstrapJar(ArtifactSpec dependency) {

        if (dependency.scope.equals("TEST")) {
            return false;
        }

        if (isExplodedBootstrap(dependency)) {
            return false;
        }

        return !dependency.scope.equals("PROVIDED");
    }

    public boolean isExplodedBootstrap(ArtifactSpec dependency) {
        if (dependency.groupId().equals(JBOSS_MODULES_GROUP_ID) && dependency.artifactId().equals(JBOSS_MODULES_ARTIFACT_ID)) {
            return true;
        }
        if (dependency.groupId().equals(WILDFLY_SWARM_GROUP_ID) && dependency.artifactId().equals(WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID)) {
            return true;
        }
        return false;
    }

    public boolean isProvidedDependency(ArtifactSpec dependency) {
        String gav = dependency.groupId() + ":" + dependency.artifactId();
        return this.providedGAVs.contains(gav);
    }

    public ArtifactSpec findWildFlySwarmBootstrapJar() {
        return findArtifact(WILDFLY_SWARM_GROUP_ID, WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID, null, "jar", null);
    }

    public ArtifactSpec findJBossModulesJar() {
        return findArtifact(JBOSS_MODULES_GROUP_ID, JBOSS_MODULES_ARTIFACT_ID, null, "jar", null);
    }

    public ArtifactSpec findArtifact(String groupId, String artifactId, String version, String packaging, String classifier) {
        for (ArtifactSpec each : this.dependencies) {
            if (groupId != null && !groupId.equals(each.groupId())) {
                continue;
            }

            if (artifactId != null && !artifactId.equals(each.artifactId())) {
                continue;
            }

            if (version != null && !version.equals(each.version())) {
                continue;
            }

            if (packaging != null && !packaging.equals(each.type())) {
                continue;
            }

            if (classifier != null && !classifier.equals(each.classifier())) {
                continue;
            }

            return each;
        }

        return null;
    }

    public void populateUberJarMavenRepository(Archive archive) throws Exception {
        Set<ArtifactSpec> dependencies = new HashSet<>();
        for (ArtifactSpec dependency : this.dependencies) {
            if (!this.bootstrapDependencies.contains(dependency) && !this.moduleDependencies.contains(dependency)) {
                dependency.shouldGather = false;
            }

            if (includeAsBootstrapJar(dependency)) {
                dependency.shouldGather = true;

            }
            if (isExplodedBootstrap(dependency)) {
                dependency.shouldGather = false;
            }

            if (isProvidedDependency(dependency)) {
                dependency.shouldGather = false;
            }

            if (dependency.shouldGather) {
                dependencies.add(dependency);
            }
        }

        for (ArtifactSpec dependency : this.moduleDependencies) {
            dependencies.add(dependency);
        }

        for (ArtifactSpec dependency : this.bootstrapDependencies) {
            if (!isExplodedBootstrap(dependency)) {
                dependencies.add(dependency);
            }
        }
        resolveAllArtifacts(dependencies);
        for (ArtifactSpec dependency : dependencies) {
            addArtifactToArchiveMavenRepository(archive, dependency);
        }
    }

    public void populateUserMavenRepository() throws Exception {
        resolveAllArtifacts(this.dependencies);
        for (ArtifactSpec each : this.moduleDependencies) {
            resolveArtifact(each);
        }
    }

    public void addArtifactToArchiveMavenRepository(Archive archive, ArtifactSpec artifact) throws Exception {

        if (artifact.gathered) {
            return;
        }
        artifact = resolveArtifact(artifact);

        StringBuilder artifactPath = new StringBuilder("m2repo/");
        artifactPath.append(artifact.repoPath(true));

        archive.add(new FileAsset(artifact.file), artifactPath.toString());

        artifact.gathered = true;

    }

    protected static Stream<ModuleAnalyzer> findModuleXmls(File file) {
        List<ModuleAnalyzer> analyzers = new ArrayList<>();
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry each = entries.nextElement();
                String name = each.getName();

                if (name.startsWith("modules/") && name.endsWith("module.xml")) {
                    InputStream in = jar.getInputStream(each);
                    analyzers.add(new ModuleAnalyzer(in));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return analyzers.stream();
    }

    Set<ArtifactSpec> getDependencies() {
        return this.dependencies;
    }

    Set<ArtifactSpec> getModuleDependencies() {
        return this.moduleDependencies;
    }

    Set<ArtifactSpec> getBootstrapDependencies() {
        return this.bootstrapDependencies;
    }

    Set<String> getBootstrapModules() {
        return this.bootstrapModules;
    }

    Set<String> getProvidedGAVs() {
        return this.providedGAVs;
    }

    Map<String, String> getProvidedGAVToModuleMappings() {
        return this.providedGAVToModuleMappings;
    }

    protected void analyzeDependencies(boolean autodetect) throws Exception {
        /*
        for (ArtifactSpec each : this.explicitDependencies) {
            System.err.println( "explicit: " + each );
        }
        */

        Set<ArtifactSpec> newDeps = resolveAllArtifacts(this.explicitDependencies);

        /*
        for (ArtifactSpec each : this.presolvedDependencies) {
            System.err.println("pre-solved: " + each);
        }
        */

        this.dependencies.clear();
        if (this.presolvedDependencies.isEmpty()) {
            this.dependencies.addAll(newDeps);
        } else {
            newDeps.stream()
                    .filter(dep -> autodetect || this.presolvedDependencies.contains(dep))
                    .forEach(dep -> this.dependencies.add(dep));
        }

        /*
        for (ArtifactSpec each : this.dependencies) {
            System.err.println("now: " + each);
        }
        */

        scanModulesDependencies();
        scanBootstrapDependencies();
        analyzeModuleDependencies();
        analyzeProvidedDependencies();
        analyzeRemovableDependencies();
    }

    protected void analyzeRemovableDependencies() throws Exception {
        Set<ArtifactSpec> bootstrapDeps = this.dependencies.stream()
                .filter(e -> isWildFlySwarmBootstrap(e.file))
                .collect(Collectors.toSet());

        Set<ArtifactSpec> nonBootstrapDeps = new HashSet<>();
        nonBootstrapDeps.addAll(this.explicitDependencies);
        nonBootstrapDeps.removeAll(bootstrapDeps);

        Set<ArtifactSpec> simplifiedDeps = resolveAllArtifacts(nonBootstrapDeps);

        this.removableDependencies.addAll(this.dependencies);
        this.removableDependencies.removeAll(simplifiedDeps);

        /*
        for (ArtifactSpec each : this.removableDependencies) {
            System.err.println( "remove: " + each );
        }
        */

    }

    public Set<ArtifactSpec> getRemovableDependencies() {
        return this.removableDependencies;
    }

    public boolean isRemovable(Node node) {
        Asset asset = node.getAsset();
        if (asset == null) {
            return false;
        }

        String path = node.getPath().get();

        try {
            byte[] checksum = checksum(asset);

            return this.removableDependencies.stream()
                    .filter(e -> path.endsWith(e.artifactId() + "-" + e.version() + ".jar"))
                    .map(e -> {
                        try {
                            return checksum(e.file);
                        } catch (IOException | NoSuchAlgorithmException | DigestException e1) {
                            return null;
                        }
                    })
                    .filter(e -> e != null)
                    .anyMatch(e -> Arrays.equals(e, checksum));
        } catch (NoSuchAlgorithmException | IOException | DigestException e) {
            e.printStackTrace();
        }

        return false;
    }

    protected byte[] checksum(Asset asset) throws IOException, DigestException, NoSuchAlgorithmException {
        try (InputStream in = asset.openStream()) {
            return checksum(in);
        }
    }

    protected byte[] checksum(File file) throws IOException, DigestException, NoSuchAlgorithmException {
        try (InputStream in = new FileInputStream(file)) {
            return checksum(in);
        }
    }

    protected byte[] checksum(InputStream in) throws IOException, NoSuchAlgorithmException, DigestException {
        byte[] buf = new byte[1024];
        int len = 0;

        MessageDigest md = MessageDigest.getInstance("SHA1");

        while ((len = in.read(buf)) >= 0) {
            md.update(buf, 0, len);
        }

        return md.digest();
    }

    protected boolean isWildFlySwarmBootstrap(File file) {
        if (file == null) {
            return false;
        }

        try (JarFile jar = new JarFile(file)) {
            return jar.getEntry("wildfly-swarm-bootstrap.conf") != null;
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    protected void scanModulesDependencies() {
        this.dependencies.stream()
                .filter(this::isModulesDependency)
                .forEach(e -> {
                    this.bootstrapDependencies.add(e);
                });
    }

    protected void scanBootstrapDependencies() {
        this.dependencies.stream()
                .filter(this::isBootstrapDependency)
                .map(spec -> spec.file)
                .forEach(this::scanBootstrapDependency);
    }

    protected boolean isModulesDependency(ArtifactSpec spec) {
        if (spec.file == null) {
            return false;
        }

        if (!spec.type().equals("jar")) {
            return false;
        }

        if (spec.groupId().equals(WILDFLY_SWARM_GROUP_ID) && spec.artifactId().equals(WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID)) {
            return true;
        }

        try (JarFile jar = new JarFile(spec.file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith("modules/") && name.endsWith("module.xml")) {
                    return true;
                }

            }
        } catch (IOException e) {
        }

        return false;
    }

    protected boolean isBootstrapDependency(ArtifactSpec spec) {
        if (spec.file == null) {
            return false;
        }
        if (!spec.type().equals("jar")) {
            return false;
        }
        try (JarFile jar = new JarFile(spec.file)) {
            ZipEntry entry = jar.getEntry("wildfly-swarm-bootstrap.conf");
            if (entry != null) {
                return true;
            }
        } catch (IOException e) {
        }

        if (spec.groupId().equals(WILDFLY_SWARM_GROUP_ID) && spec.artifactId().equals(WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID)) {
            return true;
        }
        return false;
    }

    protected void scanBootstrapDependency(File file) {
        try (JarFile jar = new JarFile(file)) {
            ZipEntry entry = jar.getEntry("wildfly-swarm-bootstrap.conf");
            if (entry != null) {

                try (InputStream in = jar.getInputStream(entry)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            this.bootstrapModules.add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    protected WildFlySwarmBootstrapConf getWildFlySwarmBootstrapConf() {
        WildFlySwarmBootstrapConf bootstrapConf = new WildFlySwarmBootstrapConf();

        for (ArtifactSpec each : this.bootstrapDependencies) {
            if (!isExplodedBootstrap(each)) {
                bootstrapConf.addEntry(each);
            }
        }

        return bootstrapConf;
    }

    protected WildFlySwarmApplicationConf getWildFlySwarmApplicationConf(ProjectAsset projectAsset) {

        Set<ArtifactSpec> applicationArtifacts = new HashSet<>();

        WildFlySwarmApplicationConf appConf = new WildFlySwarmApplicationConf();

        for (ArtifactSpec each : this.dependencies) {
            if (!this.bootstrapDependencies.contains(each)) {
                if (each.type().equals("jar") && each.shouldGather) {
                    Set<WildFlySwarmClasspathConf.Action> actions =
                            this.classpathConf.getActions(each.file, each.groupId(), each.artifactId())
                                    .stream()
                                    .filter(a -> a instanceof WildFlySwarmClasspathConf.ReplaceAction)
                                    .collect(Collectors.toSet());
                    if (actions.isEmpty()) {
                        if (!isProvidedDependency(each)) {
                            applicationArtifacts.add(each);
                        }
                    } else {
                        if (includeAsBootstrapJar(each)) {
                            for (WildFlySwarmClasspathConf.Action action : actions) {
                                WildFlySwarmClasspathConf.ReplaceAction replace = (WildFlySwarmClasspathConf.ReplaceAction) action;
                                appConf.addEntry(new WildFlySwarmApplicationConf.ModuleEntry(replace.moduleName + ":" + replace.moduleSlot));
                            }
                        }
                    }
                }
            }
        }

        for (String each : this.bootstrapModules) {
            appConf.addEntry(new WildFlySwarmApplicationConf.FractionModuleEntry(each));
        }

        for (ArtifactSpec each : applicationArtifacts) {
            String mapped = this.providedGAVToModuleMappings.get(each.groupId() + ":" + each.artifactId());
            if (mapped != null) {
                appConf.addEntry(new WildFlySwarmApplicationConf.ModuleEntry(mapped));
            } else {
                if (includeAsBootstrapJar(each)) {
                    each.shouldGather = true;
                    appConf.addEntry(new WildFlySwarmApplicationConf.GAVEntry(each));
                }
            }
        }

        if (projectAsset != null) {
            appConf.addEntry(new WildFlySwarmApplicationConf.PathEntry(projectAsset.getName()));
        }

        return appConf;

    }

    protected WildFlySwarmDependenciesConf getWildFlySwarmDependenciesConf() {
        WildFlySwarmDependenciesConf depsConf = new WildFlySwarmDependenciesConf();

        for (ArtifactSpec each : this.dependencies) {
            if (providedGAVs.contains(each.groupId() + ":" + each.artifactId())) {
                continue;
            }
            if (each.scope.equalsIgnoreCase("compile")) {
                if (each.type().equals("jar")) {
                    depsConf.addPrimaryDependency(each);
                } else {
                    depsConf.addExtraDependency(each);
                }
            }
        }

        return depsConf;
    }

    protected void analyzeModuleDependencies() {
        this.bootstrapDependencies.stream()
                .filter(e -> e.type().equals("jar"))
                .map(e -> e.file)
                .flatMap(DependencyManager::findModuleXmls)
                .forEach(this::analyzeModuleDependencies);

    }

    protected void analyzeModuleDependencies(ModuleAnalyzer analyzer) {
        this.moduleDependencies.addAll(analyzer.getDependencies());

        for (ArtifactSpec each : analyzer.getDependencies()) {
            if (analyzer.getName().startsWith("org.wildfly.swarm") && analyzer.getSlot().equals("api")) {
                providedGAVs.add(each.groupId() + ":" + each.artifactId());
                //providedGAVs.add(each.groupId() + ":" + each.artifactId().substring(0, each.artifactId().length() - "-api".length()));
            } else {
                providedGAVToModuleMappings.put(
                        each.groupId() + ":" + each.artifactId(),
                        analyzer.getName() + ":" + analyzer.getSlot());
            }
        }

    }

    protected void analyzeProvidedDependencies() {
        this.dependencies.stream()
                .filter(e -> e.type().equals("jar"))
                .forEach(this::analyzeProvidedDependencies);
    }

    protected void analyzeProvidedDependencies(ArtifactSpec spec) {
        if (spec.file == null) {
            return;
        }
        try (JarFile jar = new JarFile(spec.file)) {
            ZipEntry entry = jar.getEntry(WildFlySwarmClasspathConf.CLASSPATH_LOCATION);
            if (entry != null) {
                try ( InputStream in = jar.getInputStream(entry)) {
                    this.classpathConf.read(in);

                    // add ourselves
                    providedGAVs.add(spec.groupId() + ":" + spec.artifactId());

                    if (spec.artifactId().endsWith("-modules")) {
                        providedGAVs.add(spec.groupId() + ":" + spec.artifactId().substring(0, spec.artifactId().length() - "-modules".length()) + "-api");
                    }

                    if (spec.artifactId().endsWith("-api")) {
                        providedGAVs.add(spec.groupId() + ":" + spec.artifactId().substring(0, spec.artifactId().length() - "-api".length()));
                    }

                    providedGAVs.addAll(
                            this.classpathConf.getMatchesForActionType(WildFlySwarmClasspathConf.MavenMatcher.class, WildFlySwarmClasspathConf.RemoveAction.class).stream()
                                    .map(m -> (WildFlySwarmClasspathConf.MavenMatcher) m)
                                    .map(m -> m.groupId + ":" + m.artifactId)
                                    .collect(Collectors.toList())
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected ArtifactSpec resolveArtifact(ArtifactSpec spec) throws Exception {
        if (spec.file == null) {
            ArtifactSpec newArtifact = this.resolver.resolve(spec);

            if (newArtifact == null) {
                throw new BuildException("Unable to resolve artifact: " + spec);
            }

            spec.file = newArtifact.file;
        }

        return spec;
    }

    protected Set<ArtifactSpec> resolveAllArtifacts(Set<ArtifactSpec> specs) throws Exception {
        return this.resolver.resolveAll(specs);
    }

    private final Set<ArtifactSpec> explicitDependencies = new HashSet<>();

    private final Set<ArtifactSpec> presolvedDependencies = new HashSet<>();

    private final Set<ArtifactSpec> dependencies = new HashSet<>();

    private final Set<ArtifactSpec> moduleDependencies = new HashSet<>();

    private final Set<ArtifactSpec> bootstrapDependencies = new HashSet<>();

    private final Set<ArtifactSpec> removableDependencies = new HashSet<>();

    private final Set<String> bootstrapModules = new HashSet<>();

    private final Set<String> providedGAVs = new HashSet<>();

    private final Map<String, String> providedGAVToModuleMappings = new HashMap<>();

    private ArtifactResolvingHelper resolver;

    private WildFlySwarmClasspathConf classpathConf = new WildFlySwarmClasspathConf();

}
