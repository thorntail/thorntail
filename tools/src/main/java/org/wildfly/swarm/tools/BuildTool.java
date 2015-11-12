/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.asset.ZipFileEntryAsset;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmApplicationConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmBootstrapConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

/**
 * @author Bob McWhirter
 */
public class BuildTool {

    private final JavaArchive archive;

    private String mainClass;

    private String contextPath = "/";

    private boolean bundleDependencies = true;

    private boolean resolveTransitiveDependencies = false;

    private final Set<ArtifactSpec> dependencies = new HashSet<>();

    private final Set<ArtifactSpec> moduleDependencies = new HashSet<>();

    private final Set<String> resourceDirectories = new HashSet<>();

    //private ArtifactSpec projectArtifact;
    private ProjectAsset projectAsset;

    private ArtifactResolvingHelper resolver;

    private Properties properties = new Properties();

    private Set<ArtifactSpec> bootstrappedArtifacts = new HashSet<>();

    private Set<ArtifactSpec> coreSwarmArtifacts = new HashSet<>();

    private Set<String> bootstrappedModules = new HashSet<>();

    private Map<String, String> providedMappings = new HashMap<>();

    private Set<String> additionnalModules = new HashSet<>();

    public BuildTool() {
        this.archive = ShrinkWrap.create(JavaArchive.class);
    }

    public BuildTool mainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public BuildTool contextPath(String contextPath) {
        this.contextPath = contextPath;
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
        this.dependencies.add(new ArtifactSpec(scope, groupId, artifactId, version, packaging, classifier, file));
        return this;
    }

    public Set<ArtifactSpec> dependencies() {
        return this.dependencies;
    }

    public Set<ArtifactSpec> moduleDependencies() {
        return this.moduleDependencies;
    }

    public Set<String> additionnalModules() {
        return this.additionnalModules;
    }

    public BuildTool artifactResolvingHelper(ArtifactResolvingHelper resolver) {
        this.resolver = resolver;
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
        resolveDependencies();
        addWildflySwarmBootstrapJar();
        setupBootstrap();
        createManifest();
        createWildflySwarmProperties();
        collectDependencies();
        setupApplication();
        createDependenciesTxt();
        addAdditionnalModule();
        return this.archive;
    }

    private void addWildflySwarmBootstrapJar() throws BuildException, IOException {
        ArtifactSpec artifact = findArtifact("org.wildfly.swarm", "wildfly-swarm-bootstrap", null, "jar", null);

        this.bootstrappedArtifacts.add( artifact );

        if (!bootstrapJarShadesJBossModules(artifact.file)) {
            ArtifactSpec jbossModules = findArtifact("org.jboss.modules", "jboss-modules", null, "jar", null);
            expandArtifact(jbossModules.file);
        }
        expandArtifact(artifact.file);
    }


    private void setupBootstrap() throws Exception {
        for (ArtifactSpec each : this.dependencies) {
            try (JarFile jar = new JarFile(each.file)) {
                ZipEntry entry = jar.getEntry("wildfly-swarm-bootstrap.conf");
                if (entry != null) {
                    this.bootstrappedArtifacts.add(each);

                    try (InputStream in = jar.getInputStream(entry)) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String line = null;

                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                this.bootstrappedModules.add(line);
                            }
                        }
                    }
                }
            }
        }

        WildFlySwarmBootstrapConf bootstrapConf = new WildFlySwarmBootstrapConf();

        for (ArtifactSpec each : this.bootstrappedArtifacts) {
            bootstrapConf.addEntry( each );
            gatherDependency(each);
        }

        this.archive.add(new StringAsset(bootstrapConf.toString()), WildFlySwarmBootstrapConf.CLASSPATH_LOCATION );
    }

    private void setupApplication() throws Exception {

        Set<ArtifactSpec> applicationArtifacts = new HashSet<>();

        for (ArtifactSpec each : this.dependencies) {
            if (!this.bootstrappedArtifacts.contains(each) ) {
                if (each.type().equals("jar") && each.shouldGather) {
                    applicationArtifacts.add(each);
                }
            }
        }

        this.archive.add(this.projectAsset);

        WildFlySwarmApplicationConf appConf = new WildFlySwarmApplicationConf();

        for (String each : this.bootstrappedModules) {
            appConf.addEntry(new WildFlySwarmApplicationConf.ModuleEntry(each));
        }

        for (ArtifactSpec each : applicationArtifacts) {
            String mapped = this.providedMappings.get(each.groupId() + ":" + each.artifactId());
            if (mapped != null) {
                appConf.addEntry(new WildFlySwarmApplicationConf.ModuleEntry(mapped));
            } else {
                if (includeAsBootstrapJar(each)) {
                    gatherDependency(each);
                    appConf.addEntry(new WildFlySwarmApplicationConf.GAVEntry(each));
                }
            }
        }

        appConf.addEntry( new WildFlySwarmApplicationConf.PathEntry( this.projectAsset.getName()));
        this.archive.add(new StringAsset(appConf.toString()), WildFlySwarmApplicationConf.CLASSPATH_LOCATION );

    }

    public boolean includeAsBootstrapJar(ArtifactSpec dependency) {

        if ( dependency.scope.equals( "TEST" ) ) {
            return false;
        }
        if (dependency.groupId().equals("org.jboss.modules") && dependency.artifactId().equals("jboss-modules")) {
            return false;
        }

        return !dependency.scope.equals("PROVIDED");

        //return true;
    }

    protected boolean hasNonBootstrapMarker(ArtifactSpec spec) {
        if (spec.file != null) {

            try (JarFile jar = new JarFile(spec.file)) {
                ZipEntry entry = jar.getEntry("META-INF/wildfly-swarm-non-bootstrap.txt");
                return (entry != null);
            } catch (IOException e) {
                //e.printStackTrace();
            }

        }

        return false;
    }

    protected void resolveDependencies() throws Exception {
        if (this.resolveTransitiveDependencies) {
            Set<ArtifactSpec> newDeps = this.resolver.resolveAll(dependencies);
            this.dependencies.clear();
            this.dependencies.addAll(newDeps);
        } else {
            for (ArtifactSpec each : dependencies()) {
                resolveArtifact(each);
            }
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

    protected void gatherDependency(ArtifactSpec artifact) throws Exception {
        if ( artifact.gathered ) {
            return;
        }
        artifact = resolveArtifact(artifact);

        StringBuilder artifactPath = new StringBuilder("m2repo/");
        artifactPath.append( artifact.repoPath(true));

        this.archive.add(new FileAsset(artifact.file), artifactPath.toString());

        artifact.gathered = true;
    }

    private void createManifest() throws IOException {
        UberJarManifestAsset manifest = new UberJarManifestAsset(this.mainClass);
        this.archive.add( manifest );
    }

    private void createWildflySwarmProperties() throws IOException {

        Properties props = new Properties();

        Enumeration<?> propNames = this.properties.propertyNames();

        while (propNames.hasMoreElements()) {
            String eachName = (String) propNames.nextElement();
            String eachValue = this.properties.get(eachName).toString();
            props.put(eachName, eachValue);
        }
        //props.putAll( this.properties );

        //props.setProperty("wildfly.swarm.app.artifact", this.projectArtifact.artifactId + "-" + this.projectArtifact.version + "." + this.projectArtifact.packaging);
        props.setProperty("wildfly.swarm.app.artifact", this.projectAsset.getSimpleName());
        props.setProperty("wildfly.swarm.context.path", this.contextPath);

        ByteArrayOutputStream propsBytes = new ByteArrayOutputStream();
        props.store(propsBytes, "Generated by WildFly Swarm");

        this.archive.addAsManifestResource(new ByteArrayAsset(propsBytes.toByteArray()), "wildfly-swarm.properties");
    }


    private void createDependenciesTxt() throws IOException {
        Set<String> provided = new HashSet<>();

        for (ArtifactSpec each : this.dependencies) {
            if (each.type().equals("jar")) {
                try (JarFile jar = new JarFile(each.file)) {

                    ZipEntry entry = jar.getEntry("provided-dependencies.txt");
                    if (entry != null) {
                        // add ourselves
                        provided.add(each.groupId() + ":" + each.artifactId());

                        try (InputStream in = jar.getInputStream(entry)) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            String line = null;

                            // add everything mentioned in the file
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                if (line.length() > 0) {
                                    String[] parts = line.split("\\|");
                                    if (parts.length > 1) {
                                        this.providedMappings.put(parts[0], parts[1]);
                                    }
                                    provided.add(parts[0].trim());
                                }
                            }
                        }
                    }
                }
            }
        }

        for (String each : this.resourceDirectories) {
            Path providedDependencies = Paths.get(each, "provided-dependencies.txt");
            if (Files.exists(providedDependencies)) {

                try (InputStream in = new FileInputStream(providedDependencies.toFile())) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line = null;

                    // add everything mentioned in the file
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            String[] parts = line.split("\\|");
                            if (parts.length > 1) {
                                this.providedMappings.put(parts[0], parts[1]);
                            }
                            provided.add(parts[0].trim());
                        }
                    }
                }
            }
        }

        StringBuilder depsTxt = new StringBuilder();
        StringBuilder extraDepsTxt = new StringBuilder();

        WildFlySwarmDependenciesConf depsConf = new WildFlySwarmDependenciesConf();

        for (ArtifactSpec each : this.dependencies) {
            if (provided.contains(each.groupId() + ":" + each.artifactId())) {
                continue;
            }
            if (each.scope.equals("compile")) {
                if (each.type().equals("jar")) {
                    depsConf.addPrimaryDependency( each );
                } else {
                    depsConf.addExtraDependency( each );
                }
            }

            this.archive.add(new StringAsset(depsConf.toString()), WildFlySwarmDependenciesConf.CLASSPATH_LOCATION );
        }
    }

    protected void collectDependencies() throws Exception {
        if (!this.bundleDependencies) {
            return;
        }
        analyzeModuleDependencies();
        gatherDependencies();
    }


    protected void analyzeModuleDependencies() throws IOException {
        for (ArtifactSpec each : this.bootstrappedArtifacts) {
            this.coreSwarmArtifacts.add(each);
            analyzeModuleDependencies(each);
        }
    }

    protected void analyzeModuleDependencies(ArtifactSpec artifact) throws IOException {
        if (!artifact.type().equals("jar")) {
            return;
        }

        JarFile jar = new JarFile(artifact.file);

        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry each = entries.nextElement();
            String name = each.getName();

            if (name.startsWith("modules/") && name.endsWith("module.xml")) {
                try (InputStream in = jar.getInputStream(each)) {
                    analyzeModuleDependencies(in);
                }
            }
        }
    }

    protected void analyzeModuleDependencies(InputStream moduleXml) throws IOException {
        ModuleAnalyzer analyzer = new ModuleAnalyzer(moduleXml);
        this.moduleDependencies.addAll( analyzer.getDependencies() );
    }

    protected void gatherDependencies() throws Exception {
        this.coreSwarmArtifacts.addAll(this.moduleDependencies);

        if (this.projectAsset.getSimpleName().endsWith(".war")) {
            for (ArtifactSpec each : this.dependencies) {
                if (!this.coreSwarmArtifacts.contains(each)) {
                    each.shouldGather = false;
                }
            }
        }

        for (ArtifactSpec each : this.dependencies) {
            if (each.shouldGather) {
                gatherDependency(each);
            }
        }

        for (ArtifactSpec each : this.coreSwarmArtifacts) {
            if (each.shouldGather) {
                gatherDependency(each);
            }
        }
    }

    private File createJar(String baseName, Path dir) throws IOException {
        File out = new File(dir.toFile(), baseName + "-swarm.jar");
        ZipExporter exporter = this.archive.as(ZipExporter.class);
        exporter.exportTo(out, true);
        return out;
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

    private void addAdditionnalModule() {
        for (String additionnalModule : additionnalModules) {
            File file = new File(additionnalModule);
            this.archive.addAsResource(file, "modules");
        }
    }
}

