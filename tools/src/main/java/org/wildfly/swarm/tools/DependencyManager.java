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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
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
import org.wildfly.swarm.bootstrap.env.FractionManifest;
import org.wildfly.swarm.bootstrap.env.WildFlySwarmManifest;

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

    public Set<ArtifactSpec> getDependencies() {
        return this.dependencies;
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
            if (!isExplodedBootstrap(dependency)) {
                dependency.shouldGather = true;
            }
            if (isExplodedBootstrap(dependency)) {
                dependency.shouldGather = false;
            }
            if (dependency.shouldGather) {
                dependencies.add(dependency);
            }
        }

        for (ArtifactSpec dependency : this.moduleDependencies) {
            dependencies.add(dependency);
        }

        resolveAllArtifactsNonTransitively(dependencies);
        for (ArtifactSpec dependency : dependencies) {
            addArtifactToArchiveMavenRepository(archive, dependency);
        }
    }

    public void populateUserMavenRepository() throws Exception {
        Set<ArtifactSpec> dependencies = new HashSet<>();
        dependencies.addAll( this.dependencies );
        dependencies.addAll( this.moduleDependencies );
        resolveAllArtifactsNonTransitively( dependencies );
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

    protected void analyzeDependencies(boolean autodetect) throws Exception {
        Set<ArtifactSpec> allResolvedDependencies = resolveAllArtifactsTransitively(this.explicitDependencies);

        /*
        for (ArtifactSpec each : allResolvedDependencies) {
            System.err.println("all-resolve: " + each);
        }
        */

        this.dependencies.clear();

        if (this.presolvedDependencies.isEmpty()) {
            // add all dependencies, because we have no idea about
            // any pre-solution that might involve <exclusions> etc
            this.dependencies.addAll(allResolvedDependencies);
        } else {
            // if we're in auto-detect mode, then all mentioned
            // dependencies should be added, else, only add
            // those that were pre-solved to accomodate <exclusions>
            allResolvedDependencies.stream()
                    .filter(dep -> autodetect || this.presolvedDependencies.contains(dep))
                    .forEach(dep -> this.dependencies.add(dep));
        }

        analyzeRemovableDependencies();
        analyzeFractionManifests();

        this.dependencies.stream()
                .filter(e -> !this.removableDependencies.contains(e))
                .forEach(e -> {
                    this.applicationManifest.addDependency(e.mavenGav());
                });
        analyzeModuleDependencies();
    }

    protected void analyzeModuleDependencies() {
        this.dependencies.stream()
                .filter(e -> e.type().equals("jar"))
                .map(e -> e.file)
                .flatMap(DependencyManager::findModuleXmls)
                .forEach(this::analyzeModuleDependencies);

    }

    protected void analyzeModuleDependencies(ModuleAnalyzer analyzer) {
        this.moduleDependencies.addAll(analyzer.getDependencies());
    }

    protected void analyzeRemovableDependencies() throws Exception {
        Set<ArtifactSpec> bootstrapDeps = this.dependencies.stream()
                .filter(e -> isFractionJar(e.file))
                .collect(Collectors.toSet());

        /*
        for (ArtifactSpec each : bootstrapDeps) {
            System.err.println("bootstrap: " + each);
        }
        */

        Set<ArtifactSpec> nonBootstrapDeps = new HashSet<>();
        nonBootstrapDeps.addAll(this.explicitDependencies);
        nonBootstrapDeps.removeAll(bootstrapDeps);

        /*
        for (ArtifactSpec each : nonBootstrapDeps) {
            System.err.println("non-bootstrap: " + each);
        }
        */

        // re-resolve the application's dependencies minus
        // any of our swarm dependencies
        Set<ArtifactSpec> simplifiedDeps = resolveAllArtifactsTransitively(nonBootstrapDeps);

        /*
        for (ArtifactSpec each : simplifiedDeps) {
            System.err.println("simplified: " + each);
        }
        */

        Set<ArtifactSpec> justJars = this.dependencies
                .stream()
                .filter(e -> e.type().equals("jar"))
                .collect(Collectors.toSet());

        /*
        for (ArtifactSpec each : this.dependencies) {
            System.err.println("core: " + each);
        }
        */

        // do not remove .war or .rar or anything else weird-o like.
        this.removableDependencies.addAll(justJars);
        this.removableDependencies.removeAll(simplifiedDeps);

        /*
        for (ArtifactSpec each : this.removableDependencies) {
            System.err.println("removable: " + each);
        }
        */
    }

    protected void analyzeFractionManifests() {
        this.dependencies.stream()
                .map(e -> fractionManifest(e.file))
                .filter(e -> e != null)
                .forEach((manifest) -> {
                    String module = manifest.getModule();
                    if (module != null) {
                        this.applicationManifest.addBootstrapModule(module);
                    }
                });

        this.dependencies.stream()
                .filter(e -> isFractionJar(e.file) || isConfigApiModulesJar(e.file))
                .forEach((artifact) -> {
                    this.applicationManifest.addBootstrapArtifact(artifact.mavenGav());
                });

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
            byte[] checksum = checksum(asset.openStream());

            return this.removableDependencies.stream()
                    .filter(e -> path.endsWith(e.artifactId() + "-" + e.version() + ".jar"))
                    .map(e -> {
                        try {
                            return checksum(new FileInputStream(e.file));
                        } catch (IOException | NoSuchAlgorithmException | DigestException e1) {
                            return null;
                        }
                    })
                    .filter(e -> e != null)
                    .anyMatch(e -> {
                        return Arrays.equals(e, checksum);
                    });
        } catch (NoSuchAlgorithmException | IOException | DigestException e) {
            e.printStackTrace();
        }

        return false;
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

    protected boolean isConfigApiModulesJar(File file) {
        if (file == null) {
            return false;
        }

        try (JarFile jar = new JarFile(file)) {
            return jar.getEntry("wildfly-swarm-modules.conf") != null;
        } catch (IOException e) {
            // ignore
        }
        return false;

    }

    protected boolean isFractionJar(File file) {
        if (file == null) {
            return false;
        }

        try (JarFile jar = new JarFile(file)) {
            return jar.getEntry(FractionManifest.CLASSPATH_LOCATION) != null;
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    protected FractionManifest fractionManifest(File file) {
        try (JarFile jar = new JarFile(file)) {
            ZipEntry entry = jar.getEntry(FractionManifest.CLASSPATH_LOCATION);
            if (entry != null) {
                try (InputStream in = jar.getInputStream(entry)) {
                    return new FractionManifest(in);
                }
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    void setProjectAsset(ProjectAsset projectAsset) {
        if (!this.applicationManifest.isHollow()) {
            this.projectAsset = projectAsset;
            this.applicationManifest.setAsset(this.projectAsset.getName());
        }
    }

    protected WildFlySwarmManifest getWildFlySwarmManifest() {
        return this.applicationManifest;
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

    protected Set<ArtifactSpec> resolveAllArtifactsTransitively(Set<ArtifactSpec> specs) throws Exception {
        return this.resolver.resolveAll(specs);
    }

    protected Set<ArtifactSpec> resolveAllArtifactsNonTransitively(Set<ArtifactSpec> specs) throws Exception {
        return this.resolver.resolveAll(specs, false);
    }

    private final WildFlySwarmManifest applicationManifest = new WildFlySwarmManifest();

    private final Set<ArtifactSpec> dependencies = new HashSet<>();

    private final Set<ArtifactSpec> removableDependencies = new HashSet<>();

    private final Set<ArtifactSpec> moduleDependencies = new HashSet<>();

    private final Set<ArtifactSpec> explicitDependencies = new HashSet<>();

    private final Set<ArtifactSpec> presolvedDependencies = new HashSet<>();

    private ProjectAsset projectAsset;

    private ArtifactResolvingHelper resolver;

}
