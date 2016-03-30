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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmApplicationConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmBootstrapConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

/**
 * @author Bob McWhirter
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

    public void addDependency(ArtifactSpec dep) {
        this.dependencies.add(dep);
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

            } if (isExplodedBootstrap(dependency)) {
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
        Set<ArtifactSpec> deps = new HashSet<>(this.dependencies);
        deps.addAll(this.moduleDependencies);
        resolveAllArtifacts(deps);
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
        try {
            JarFile jar = new JarFile(file);
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

    protected void analyzeDependencies(boolean resolveTransitive) throws Exception {
        if (resolveTransitive) {
            Set<ArtifactSpec> newDeps = this.resolver.resolveAll(this.dependencies);
            this.dependencies.clear();
            this.dependencies.addAll(newDeps);
        } else {
            this.resolver.resolveAll(this.dependencies);
        }

        scanModulesDependencies();
        scanBootstrapDependencies();
        analyzeModuleDependencies();
        analyzeProvidedDependencies();
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

        for (ArtifactSpec each : this.dependencies) {
            if (!this.bootstrapDependencies.contains(each)) {
                if (each.type().equals("jar") && each.shouldGather) {
                    applicationArtifacts.add(each);
                }
            }
        }

        WildFlySwarmApplicationConf appConf = new WildFlySwarmApplicationConf();

        for (String each : this.bootstrapModules) {
            appConf.addEntry(new WildFlySwarmApplicationConf.ModuleEntry(each));
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

        appConf.addEntry(new WildFlySwarmApplicationConf.PathEntry(projectAsset.getName()));

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
            providedGAVToModuleMappings.put(
                    each.groupId() + ":" + each.artifactId(),
                    analyzer.getName() + ":" + analyzer.getSlot());
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

            ZipEntry entry = jar.getEntry("provided-dependencies.txt");
            if (entry != null) {
                // add ourselves
                providedGAVs.add(spec.groupId() + ":" + spec.artifactId());

                if (spec.artifactId().endsWith("-modules")) {
                    providedGAVs.add(spec.groupId() + ":" + spec.artifactId().substring(0, spec.artifactId().length() - "-modules".length()) + "-api");
                }

                try (InputStream in = jar.getInputStream(entry)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line = null;

                    // add everything mentioned in the file
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            String[] parts = line.split("\\|");
                            if (parts.length > 1) {
                                this.providedGAVToModuleMappings.put(parts[0], parts[1]);
                            }
                            providedGAVs.add(parts[0].trim());
                        }
                    }
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

    private final Set<ArtifactSpec> dependencies = new HashSet<>();

    private final Set<ArtifactSpec> moduleDependencies = new HashSet<>();

    private final Set<ArtifactSpec> bootstrapDependencies = new HashSet<>();

    private final Set<String> bootstrapModules = new HashSet<>();

    private final Set<String> providedGAVs = new HashSet<>();

    private final Map<String, String> providedGAVToModuleMappings = new HashMap<>();

    private ArtifactResolvingHelper resolver;


}
