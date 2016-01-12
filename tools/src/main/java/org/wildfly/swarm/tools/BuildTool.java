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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.asset.ZipFileEntryAsset;
import org.wildfly.swarm.bootstrap.util.CommonProperties;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmApplicationConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmBootstrapConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

/**
 * @author Bob McWhirter
 */
public class BuildTool {

    private final JavaArchive archive;

    private String mainClass;

    private boolean bundleDependencies = true;

    private boolean resolveTransitiveDependencies = false;

    private DependencyManager dependencyManager = new DependencyManager();

    private final Set<String> resourceDirectories = new HashSet<>();

    private ProjectAsset projectAsset;

    private Properties properties = new Properties();

    private Set<String> additionalModules = new HashSet<>();

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
        this.projectAsset = new ArtifactAsset(new ArtifactSpec(null, groupId, artifactId, version, packaging, null, file));
        return this;
    }

    public BuildTool projectArchive(Archive archive) {
        this.projectAsset = new ArchiveAsset(archive);
        return this;
    }

    public BuildTool dependency(String scope, String groupId, String artifactId, String version, String packaging, String classifier, File file) {
        this.dependencyManager.addDependency(new ArtifactSpec(scope, groupId, artifactId, version, packaging, classifier, file));
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

    public File build(String baseName, Path dir) throws Exception {
        build();
        return createJar(baseName, dir);
    }

    public Archive build() throws Exception {
        analyzeDependencies();
        removeSwarmDependencies();
        addWildflySwarmBootstrapJar();
        addWildFlyBootstrapConf();
        addManifest();
        addWildFlySwarmProperties();
        addWildFlySwarmApplicationConf();
        addWildFlySwarmDependenciesConf();
        addAdditionalModules();
        populateUberJarMavenRepository();

        return this.archive;
    }

    protected void analyzeDependencies() throws Exception {
        this.dependencyManager.analyzeDependencies(this.resolveTransitiveDependencies);
    }

    protected static final String WEB_INF_LIB = "/WEB-INF/lib/";

    protected static final String SWARM_ARTIFACT_MARKER = "/META-INF/maven/org.wildfly.swarm";

    protected boolean nodeIsSwarmArtifact(Node node) {
        try (final InputStream in = node.getAsset().openStream()) {

            return ShrinkWrap.create(ZipImporter.class)
                    .importFrom(in)
                    .as(JavaArchive.class)
                    .contains(SWARM_ARTIFACT_MARKER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean nodeIsInJarList(Node node, Set<String> jarList) {
        return jarList.contains(node.getPath().get().substring(WEB_INF_LIB.length()));
    }

    protected void removeSwarmDependencies() {
        final Archive<?> archive = this.projectAsset.getArchive();
        final Set<String> moduleJars = this.dependencyManager.getModuleDependencies().stream()
                .map(ArtifactSpec::jarName)
                .collect(Collectors.toSet());

        archive.getContent().values().stream()
                .filter(node -> node.getPath().get().startsWith(WEB_INF_LIB))
                .filter(node -> nodeIsInJarList(node, moduleJars) || nodeIsSwarmArtifact(node))
                .forEach(node -> archive.delete(node.getPath()));
    }

    private void addWildflySwarmBootstrapJar() throws BuildException, IOException {
        ArtifactSpec artifact = this.dependencyManager.findWildFlySwarmBootstrapJar();

        if ( artifact == null ) {
            throw new BuildException( "Unable to load org.wildfly.swarm:bootstrap; check your dependencies" );
        }

        if (!bootstrapJarShadesJBossModules(artifact.file)) {
            ArtifactSpec jbossModules = this.dependencyManager.findJBossModulesJar();
            expandArtifact(jbossModules.file);
        }
        expandArtifact(artifact.file);
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
        props.setProperty(CommonProperties.APP_ARTIFACT, this.projectAsset.getSimpleName());
        if (System.getProperty(CommonProperties.CONTEXT_PATH) == null) {
            props.setProperty(CommonProperties.CONTEXT_PATH, "/");
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
        this.archive.add(this.projectAsset);
    }


    private File createJar(String baseName, Path dir) throws IOException {
        File out = new File(dir.toFile(), baseName + "-swarm.jar");
        out.getParentFile().mkdirs();
        ZipExporter exporter = this.archive.as(ZipExporter.class);
        exporter.exportTo(out, true);
        return out;
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
        if ( this.bundleDependencies ) {
            this.dependencyManager.populateUberJarMavenRepository( this.archive );
        } else {
            this.dependencyManager.populateUserMavenRepository();
        }
    }
}

