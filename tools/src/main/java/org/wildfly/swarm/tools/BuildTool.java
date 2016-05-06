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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.asset.ZipFileEntryAsset;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmApplicationConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmBootstrapConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

/**
 * @author Bob McWhirter
 */
public class BuildTool {
    public enum FractionDetectionMode {when_missing, force, never}

    public BuildTool() {
        this.archive = ShrinkWrap.create(JavaArchive.class);
    }

    public BuildTool mainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public BuildTool properties(Properties properties) {
        this.properties.putAll(properties);
        return this;
    }

    public BuildTool bundleDependencies(boolean bundleDependencies) {
        this.bundleDependencies = bundleDependencies;
        return this;
    }

    public BuildTool resolveTransitiveDependencies(boolean resolveTransitiveDependencies) {
        this.resolveTransitiveDependencies = resolveTransitiveDependencies;
        return this;
    }

    public BuildTool projectArtifact(String groupId, String artifactId, String version, String packaging, File file) {
        projectArtifact(groupId, artifactId, version, packaging, file, null);

        return this;
    }

    public BuildTool projectArtifact(String groupId, String artifactId, String version,
                                     String packaging, File file, String artifactName) {
        this.projectAsset = new ArtifactAsset(new ArtifactSpec(null, groupId, artifactId, version, packaging, null, file),
                                              artifactName);

        return this;
    }

    public BuildTool projectArchive(Archive archive) {
        this.projectAsset = new ArchiveAsset(archive);
        return this;
    }

    public BuildTool fraction(ArtifactSpec spec) {
        this.fractions.add(spec);

        return this;
    }

    public BuildTool dependency(String scope, String groupId, String artifactId, String version,
                                String packaging, String classifier, File file) {
        dependency(new ArtifactSpec(scope, groupId, artifactId, version,
                                    packaging, classifier, file));

        return this;
    }

    public BuildTool dependency(final ArtifactSpec spec) {
        this.dependencyManager.addDependency(spec);

        return this;
    }

    public BuildTool additionalModule(String module) {
        this.additionalModules.add(module);

        return this;
    }

    public BuildTool additionalModules(Collection<String> modules) {
        this.additionalModules.addAll(modules);

        return this;
    }

    public BuildTool artifactResolvingHelper(ArtifactResolvingHelper resolver) {
        this.dependencyManager.setArtifactResolvingHelper(resolver);
        return this;
    }

    public BuildTool resourceDirectory(String dir) {
        this.resourceDirectories.add(dir);
        return this;
    }

    public BuildTool fractionList(FractionList v) {
        this.fractionList = v;

        return this;
    }

    public BuildTool fractionDetectionMode(FractionDetectionMode v) {
        this.fractionDetectionMode = v;

        return this;
    }

    public BuildTool logger(SimpleLogger logger) {
        this.log = logger;

        return this;
    }

    public File build(String baseName, Path dir) throws Exception {
        build();
        return createJar(baseName, dir);
    }

    public Archive build() throws Exception {
        analyzeDependencies();
        addWildflySwarmBootstrapJar();
        addWildFlyBootstrapConf();
        addManifest();
        addWildFlySwarmProperties();
        addWildFlySwarmApplicationConf();
        addWildFlySwarmDependenciesConf();
        addAdditionalModules();
        addProjectAsset();
        populateUberJarMavenRepository();

        return this.archive;
    }

    public boolean bootstrapJarShadesJBossModules(File artifactFile) throws IOException {
        JarFile jarFile = new JarFile(artifactFile);
        Enumeration<JarEntry> entries = jarFile.entries();

        boolean jbossModulesFound = false;

        while (entries.hasMoreElements()) {
            JarEntry each = entries.nextElement();
            if (each.getName().startsWith("org/jboss/modules/ModuleLoader")) {
                jbossModulesFound = true;
            }
        }

        return jbossModulesFound;
    }

    public void expandArtifact(File artifactFile) throws IOException {
        JarFile jarFile = new JarFile(artifactFile);
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry each = entries.nextElement();
            if (each.getName().startsWith("META-INF")) {
                continue;
            }
            if (each.isDirectory()) {
                continue;
            }
            this.archive.add(new ZipFileEntryAsset(jarFile, each), each.getName());
        }
    }

    protected void analyzeDependencies() throws Exception {
        this.dependencyManager.analyzeDependencies(this.resolveTransitiveDependencies);
    }

    private void addProjectAsset() {
        this.archive.add(this.projectAsset);
    }

    private void detectFractions() throws Exception {
        final File tmpFile = File.createTempFile("buildtool", this.projectAsset.getName().replace("/", "_"));
        tmpFile.deleteOnExit();
        this.projectAsset.getArchive().as(ZipExporter.class).exportTo(tmpFile, true);
        final FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer(this.fractionList)
                .source(tmpFile);
        this.dependencyManager.getDependencies().stream()
                .filter(d -> !"provided".equals(d.scope) && !"test".equals(d.scope))
                .forEach(d -> analyzer.source(d.file));

        final Set<FractionDescriptor> detectedFractions = analyzer.detectNeededFractions();
        this.log.info("Detected fractions: " + String.join(", ",
                                                           detectedFractions.stream()
                                                                   .map(FractionDescriptor::av)
                                                                   .sorted()
                                                                   .collect(Collectors.toList())));
        detectedFractions.stream()
                .map(FractionDescriptor::toArtifactSpec)
                .forEach(this::fraction);
    }

    static String strippedSwarmGav(MavenArtifactDescriptor desc) {
        if (desc.groupId().equals(DependencyManager.WILDFLY_SWARM_GROUP_ID)) {
            return String.format("%s:%s", desc.artifactId(), desc.version());
        }

        return desc.mscGav();
    }

    private void addFractions() throws Exception {
        final Set<ArtifactSpec> allFractions = new HashSet<>(this.fractions);
        this.fractions.stream()
                .flatMap(s -> this.fractionList.getFractionDescriptor(s.groupId(), s.artifactId())
                        .getDependencies()
                        .stream()
                        .map(FractionDescriptor::toArtifactSpec))
                .filter(d -> this.dependencyManager.findArtifact(d.groupId(), d.artifactId(), null, null, null) == null)
                .forEach(allFractions::add);

        this.log.info("Adding fractions: " +
                              String.join(", ", allFractions.stream()
                                      .map(BuildTool::strippedSwarmGav)
                                      .sorted()
                                      .collect(Collectors.toList())));

        allFractions.forEach(f -> this.dependencyManager.addDependency(f));
        resolveTransitiveDependencies(true);
        analyzeDependencies();
    }

    private void addWildflySwarmBootstrapJar() throws Exception {
        ArtifactSpec artifact = this.dependencyManager.findWildFlySwarmBootstrapJar();

        if (this.fractionDetectionMode != FractionDetectionMode.never) {
            if (this.fractionList == null) {
                throw new IllegalStateException("Fraction detection requested, but no FractionList provided");
            }

            if (this.fractionDetectionMode == FractionDetectionMode.force ||
                    artifact == null) {
                this.log.info("Scanning for needed WildFly Swarm fractions with mode: " + this.fractionDetectionMode);
                detectFractions();
                addFractions();
                artifact = this.dependencyManager.findWildFlySwarmBootstrapJar();
            }
        } else if (artifact == null) {
            this.log.error("No WildFly Swarm dependencies found and fraction detection disabled");
        }

        if (artifact != null) {
            if (!bootstrapJarShadesJBossModules(artifact.file)) {
                ArtifactSpec jbossModules = this.dependencyManager.findJBossModulesJar();
                expandArtifact(jbossModules.file);
            }
            expandArtifact(artifact.file);
        } else {
            throw new IllegalStateException("No WildFly Swarm Bootstrap fraction found");
        }
    }

    private void addManifest() throws IOException {
        UberJarManifestAsset manifest = new UberJarManifestAsset(this.mainClass);
        this.archive.add(manifest);
    }

    private void addWildFlySwarmProperties() throws IOException {
        Properties props = new Properties();

        Enumeration<?> propNames = this.properties.propertyNames();

        while (propNames.hasMoreElements()) {
            String eachName = (String) propNames.nextElement();
            String eachValue = this.properties.get(eachName).toString();
            props.put(eachName, eachValue);
        }
        props.setProperty(BootstrapProperties.APP_ARTIFACT, this.projectAsset.getSimpleName());

        if (this.bundleDependencies) {
            props.setProperty(BootstrapProperties.BUNDLED_DEPENDENCIES, "true");
        }

        ByteArrayOutputStream propsBytes = new ByteArrayOutputStream();
        props.store(propsBytes, "Generated by WildFly Swarm");

        this.archive.addAsManifestResource(new ByteArrayAsset(propsBytes.toByteArray()), "wildfly-swarm.properties");
    }

    private void addWildFlyBootstrapConf() throws Exception {
        WildFlySwarmBootstrapConf bootstrapConf = this.dependencyManager.getWildFlySwarmBootstrapConf();
        this.archive.add(new StringAsset(bootstrapConf.toString()), WildFlySwarmBootstrapConf.CLASSPATH_LOCATION);
    }

    private void addWildFlySwarmDependenciesConf() throws IOException {
        WildFlySwarmDependenciesConf depsConf = this.dependencyManager.getWildFlySwarmDependenciesConf();
        this.archive.add(new StringAsset(depsConf.toString()), WildFlySwarmDependenciesConf.CLASSPATH_LOCATION);
    }

    private void addWildFlySwarmApplicationConf() throws Exception {
        WildFlySwarmApplicationConf appConf = this.dependencyManager.getWildFlySwarmApplicationConf(this.projectAsset);
        this.archive.add(new StringAsset(appConf.toString()), WildFlySwarmApplicationConf.CLASSPATH_LOCATION);
    }

    private File createJar(String baseName, Path dir) throws IOException {
        File out = new File(dir.toFile(), baseName + "-swarm.jar");
        out.getParentFile().mkdirs();
        ZipExporter exporter = this.archive.as(ZipExporter.class);
        exporter.exportTo(out, true);
        return out;
    }

    private void addAdditionalModules() throws IOException {
        for (String additionalModule : additionalModules) {
            final File moduleDir = new File(additionalModule);
            this.archive.addAsResource(moduleDir, "modules");
            Files.find(moduleDir.toPath(), 20,
                       (p, __) -> p.getFileName().toString().equals("module.xml"))
                    .forEach(p -> this.dependencyManager.addAdditionalModule(p));

        }
    }

    private void populateUberJarMavenRepository() throws Exception {
        if (this.bundleDependencies) {
            this.dependencyManager.populateUberJarMavenRepository(this.archive);
        } else {
            this.dependencyManager.populateUserMavenRepository();
        }
    }

    private final Set<ArtifactSpec> fractions = new HashSet<>();

    private final JavaArchive archive;

    private final Set<String> resourceDirectories = new HashSet<>();

    private String mainClass;

    private boolean bundleDependencies = true;

    private boolean resolveTransitiveDependencies = false;

    private DependencyManager dependencyManager = new DependencyManager();

    private ProjectAsset projectAsset;

    private Properties properties = new Properties();

    private Set<String> additionalModules = new HashSet<>();

    private FractionDetectionMode fractionDetectionMode = FractionDetectionMode.when_missing;

    private FractionList fractionList = null;

    private SimpleLogger log = STD_LOGGER;

    private static SimpleLogger STD_LOGGER = new SimpleLogger() {
        @Override
        public void info(String msg) {
            System.out.println(msg);
        }

        @Override
        public void error(String msg) {
            System.err.println(msg);
        }

        @Override
        public void error(String msg, Throwable t) {
            error(msg);
            t.printStackTrace();
        }
    };

    public interface SimpleLogger {
        void info(String msg);

        void error(String msg);

        void error(String msg, Throwable t);
    }


}

