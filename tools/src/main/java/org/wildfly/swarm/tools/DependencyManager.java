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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.wildfly.swarm.bootstrap.env.FractionManifest;
import org.wildfly.swarm.bootstrap.env.WildFlySwarmManifest;
import org.wildfly.swarm.fractions.FractionDescriptor;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 * @author Heiko Braun
 */
public class DependencyManager implements ResolvedDependencies {

    public DependencyManager(ArtifactResolver resolver) {
        this.resolver = resolver;
    }

    public void addAdditionalModule(Path module) {
        try {
            analyzeModuleDependencies(new ModuleAnalyzer(module));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<ArtifactSpec> getDependencies() {
        return this.dependencies;
    }

    @Override
    public ArtifactSpec findWildFlySwarmBootstrapJar() {
        return findArtifact(FractionDescriptor.THORNTAIL_GROUP_ID, WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID, null, JAR, null, false);
    }

    @Override
    public ArtifactSpec findJBossModulesJar() {
        return findArtifact(JBOSS_MODULES_GROUP_ID, JBOSS_MODULES_ARTIFACT_ID, null, JAR, null, false);
    }

    @Override
    public ArtifactSpec findArtifact(String groupId, String artifactId, String version, String packaging, String classifier) {
        return findArtifact(groupId, artifactId, version, packaging, classifier, true);
    }

    @Override
    public ArtifactSpec findArtifact(String groupId, String artifactId, String version, String packaging, String classifier, boolean includeTestScope) {
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

            if (!includeTestScope && each.scope.equals("test")) {
                continue;
            }

            return each;
        }

        return null;
    }

    public ResolvedDependencies analyzeDependencies(boolean autodetect, DeclaredDependencies declaredDependencies) throws Exception {

        // resolve to local files
        resolveDependencies(declaredDependencies, autodetect);

        // sort out removals, modules, etc
        analyzeRemovableDependencies(declaredDependencies);
        analyzeFractionManifests(declaredDependencies);

        this.dependencies.stream()
                .filter(e -> !this.removableDependencies.contains(e))
                .forEach(e -> {
                    this.applicationManifest.addDependency(e.mavenGav());
                });

        analyzeModuleDependencies(declaredDependencies);

        return this;
    }

    /**
     * Resolve declared dependencies to local files, aka turning them into @{@link ResolvedDependencies)
     *
     * @param declaredDependencies
     * @throws Exception
     */
    private void resolveDependencies(DeclaredDependencies declaredDependencies, boolean autodetect) throws Exception {
        this.dependencies.clear();

        // resolve the explicit deps to local files
        // expand to transitive if these are not pre-solved
        boolean resolveExplicitsTransitively = !declaredDependencies.isPresolved() || autodetect;
        Collection<ArtifactSpec> resolvedExplicitDependencies = resolveExplicitsTransitively ?
                resolver.resolveAllArtifactsTransitively(declaredDependencies.getExplicitDependencies(), false) :
                resolver.resolveAllArtifactsNonTransitively(declaredDependencies.getExplicitDependencies());

        this.dependencies.addAll(resolvedExplicitDependencies);

        // resolve transitives if not pre-computed (i.e. from maven/gradle plugin)
        if (declaredDependencies.getTransientDependencies().isEmpty()) {

            Collection<ArtifactSpec> inputSet = declaredDependencies.getExplicitDependencies();
            Collection<ArtifactSpec> filtered = inputSet
                    .stream()
                    .filter(dep -> dep.type().equals(JAR)) // filter out composite types, like ear, war, etc
                    .collect(Collectors.toList());

            Collection<ArtifactSpec> resolvedTransientDependencies = resolver.resolveAllArtifactsTransitively(
                    filtered, false
            );

            this.dependencies.addAll(resolvedTransientDependencies);

            // add the remaining transitive ones that have not been filtered
            Collection<ArtifactSpec> remainder = new ArrayList<>();
            inputSet.forEach(remainder::add);
            remainder.removeAll(resolvedTransientDependencies);

            this.dependencies.addAll(
                    resolver.resolveAllArtifactsNonTransitively(remainder)
            );
        } else {
            // if transitive deps are pre-computed, resolve them to local files if needed
            Collection<ArtifactSpec> inputSet = declaredDependencies.getTransientDependencies();
            Collection<ArtifactSpec> filtered = inputSet
                    .stream()
                    .filter(dep -> dep.type().equals(JAR))
                    .collect(Collectors.toList());

            Collection<ArtifactSpec> resolvedTransientDependencies = Collections.emptySet();
            if (filtered.size() > 0) {

                resolvedTransientDependencies = resolver.resolveAllArtifactsNonTransitively(filtered);
                this.dependencies.addAll(resolvedTransientDependencies);
            }

            // add the remaining transitive ones that have not been filtered
            Collection<ArtifactSpec> remainder = new ArrayList<>();
            inputSet.forEach(remainder::add);
            remainder.removeAll(resolvedTransientDependencies);

            this.dependencies.addAll(
                    resolver.resolveAllArtifactsNonTransitively(remainder)
            );
        }

    }

    private void analyzeModuleDependencies(DeclaredDependencies declaredDependencies) {
        this.dependencies.stream()
                .filter(e -> e.type().equals(JAR))
                .map(e -> e.file)
                .flatMap(ResolvedDependencies::findModuleXmls)
                .forEach(this::analyzeModuleDependencies);

    }

    private void analyzeModuleDependencies(ModuleAnalyzer analyzer) {
        this.moduleDependencies.addAll(analyzer.getDependencies());
    }

    /**
     * Removable are basically all dependencies that are brought in by fractions.
     */
    private void analyzeRemovableDependencies(DeclaredDependencies declaredDependencies) throws Exception {

        Collection<ArtifactSpec> bootstrapDeps = this.dependencies.stream()
                .filter(e -> isFractionJar(e.file))
                .collect(Collectors.toSet());

        List<ArtifactSpec> nonBootstrapDeps = new ArrayList<>();
        nonBootstrapDeps.addAll(declaredDependencies.getExplicitDependencies());
        nonBootstrapDeps.removeAll(bootstrapDeps);

        // re-resolve the application's dependencies minus any of our swarm dependencies
        // [hb] TODO this can be improved to use the previous results if the data-structure allows to reason about the parent of transitive deps
        Collection<ArtifactSpec> nonBootstrapTransitive = resolver.resolveAllArtifactsTransitively(nonBootstrapDeps, true);

        // do not remove .war or .rar or anything else weird-o like.
        Set<ArtifactSpec> justJars = this.dependencies
                .stream()
                .filter(e -> e.type().equals(JAR))
                .collect(Collectors.toSet());

        this.removableDependencies.addAll(justJars);
        this.removableDependencies.removeAll(nonBootstrapTransitive);

    }

    private void analyzeFractionManifests(DeclaredDependencies declaredDependencies) {
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

    Set<ArtifactSpec> getRemovableDependencies() {
        return this.removableDependencies;
    }

    @Override
    public boolean isRemovable(Node node) {
        Asset asset = node.getAsset();
        if (asset == null) {
            return false;
        }

        String path = node.getPath().get();
        try (final InputStream inputStream = asset.openStream()) {
            byte[] checksum = checksum(inputStream);

            return this.removableDependencies.stream()
                    .filter(e -> path.endsWith(e.artifactId() + "-" + e.version() + ".jar"))
                    .map(e -> {
                        try (final FileInputStream in = new FileInputStream(e.file)) {
                            return checksum(in);
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

    public static boolean isFractionJar(File file) {
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

    @Override
    public Set<ArtifactSpec> getModuleDependencies() {
        return moduleDependencies;
    }

    private static final String JAR = "jar";

    private final WildFlySwarmManifest applicationManifest = new WildFlySwarmManifest();

    private final Set<ArtifactSpec> dependencies = new HashSet<>();

    private final Set<ArtifactSpec> removableDependencies = new HashSet<>();

    private final Set<ArtifactSpec> moduleDependencies = new HashSet<>();

    private ProjectAsset projectAsset;

    private ArtifactResolver resolver;

}
