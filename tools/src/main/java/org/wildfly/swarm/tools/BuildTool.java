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
import java.util.HashSet;
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

/**
 * @author Bob McWhirter
 */
public class BuildTool {

    private final JavaArchive archive;

    private String mainClass;

    private String contextPath = "/";

    private boolean bundleDependencies = true;

    private final Set<ArtifactSpec> dependencies = new HashSet<>();

    private final Set<ArtifactSpec> moduleDependencies = new HashSet<>();

    private final Set<String> resourceDirectories = new HashSet<>();

    //private ArtifactSpec projectArtifact;
    private ProjectAsset projectAsset;

    private ArtifactResolvingHelper resolver;

    private Properties properties = new Properties();

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

    public BuildTool projectArtifact(String groupId, String artifactId, String version, String packaging, File file) {
        this.projectAsset = new ArtifactAsset( new ArtifactSpec( null, groupId, artifactId, version, packaging, null, file ) );
        return this;
    }

    public BuildTool projectArchive(Archive archive) {
        this.projectAsset = new ArchiveAsset( archive );
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
        addWildflySwarmBootstrapJar();
        addBootstrapJars();
        createManifest();
        createWildflySwarmProperties();
        createDependenciesTxt();
        collectDependencies();
        return this.archive;
    }

    private void addWildflySwarmBootstrapJar() throws BuildException, IOException {
        ArtifactSpec artifact = findArtifact("org.wildfly.swarm", "wildfly-swarm-bootstrap", null, "jar", null);

        if ( ! bootstrapJarShadesJBossModules( artifact.file ) ) {
            ArtifactSpec jbossModules = findArtifact("org.jboss.modules", "jboss-modules", null, "jar", null);
            expandArtifact( jbossModules.file );
        }
        expandArtifact(artifact.file);
    }


    private void addBootstrapJars() throws Exception {

        Set<String> bootstrapGavs = new HashSet<>();

        for (ArtifactSpec each : this.dependencies) {
            if (includeAsBootstrapJar(each)) {
                gatherDependency(each);
                if (each.packaging.equals("jar")) {
                    if (each.classifier == null || each.classifier.equals("")) {
                        bootstrapGavs.add(each.groupId + ":" + each.artifactId + ":" + each.version);
                    } else {
                        bootstrapGavs.add(each.groupId + ":" + each.artifactId + ":" + each.version + ":" + each.classifier);
                    }
                }
            }
        }

        this.archive.add( this.projectAsset );

        StringBuilder bootstrapTxt = new StringBuilder();
        for (String each : bootstrapGavs) {
            bootstrapTxt.append("gav:").append(each).append("\n");
        }
        bootstrapTxt.append("path:").append(this.projectAsset.getName()).append("\n");
        this.archive.add(new StringAsset(bootstrapTxt.toString()), "META-INF/wildfly-swarm-bootstrap.txt");

    }


    public boolean includeAsBootstrapJar(ArtifactSpec dependency) {
        // TODO figure out a better more generic way
        if (dependency.groupId.equals("org.wildfly.swarm") && dependency.artifactId.equals("wildfly-swarm-bootstrap")) {
            return false;
        }

        if (dependency.groupId.equals("org.wildfly.swarm")) {
            return true;
        }

        if (dependency.groupId.equals("org.jboss.shrinkwrap")) {
            return true;
        }

        if (dependency.groupId.equals("org.jboss.msc") && dependency.artifactId.equals("jboss-msc")) {
            return false;
        }

        if (dependency.groupId.equals("org.jboss.modules") && dependency.artifactId.equals("jboss-modules")) {
            return false;
        }

        return !dependency.scope.equals("provided");
    }

    protected void gatherDependency(ArtifactSpec artifact) throws Exception {
        ArtifactSpec originalArtifact = artifact;
        if (artifact.file == null) {
            artifact = this.resolver.resolve(artifact);
        }

        if (artifact == null) {
            throw new BuildException("Unable to resolve artifact: " + originalArtifact);
        }

        StringBuilder artifactPath = new StringBuilder("m2repo");

        String[] groupIdParts = artifact.groupId.split("\\.");

        for (int i = 0; i < groupIdParts.length; ++i) {
            artifactPath.append('/').append(groupIdParts[i]);
        }

        artifactPath.append('/').append(artifact.artifactId);
        artifactPath.append('/').append(artifact.version);
        artifactPath.append('/').append(artifact.file.getName());

        this.archive.add(new FileAsset(artifact.file), artifactPath.toString());
    }

    private void createManifest() throws IOException {
        Manifest manifest = new Manifest();

        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, "org.wildfly.swarm.bootstrap.Main");
        if (this.mainClass != null && !this.mainClass.equals("")) {
            attrs.put(new Attributes.Name("Wildfly-Swarm-Main-Class"), this.mainClass);
        }
        //attrs.putValue("Application-Artifact", this.projectArtifact.file.getName());

        ByteArrayOutputStream manifestBytes = new ByteArrayOutputStream();
        manifest.write(manifestBytes);
        this.archive.addAsManifestResource(new ByteArrayAsset(manifestBytes.toByteArray()), "MANIFEST.MF");
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
        props.setProperty("wildfly.swarm.app.artifact", this.projectAsset.getSimpleName() );
        props.setProperty("wildfly.swarm.context.path", this.contextPath);

        ByteArrayOutputStream propsBytes = new ByteArrayOutputStream();
        props.store(propsBytes, "Generated by WildFly Swarm");

        this.archive.addAsManifestResource(new ByteArrayAsset(propsBytes.toByteArray()), "wildfly-swarm.properties");
    }


    private void createDependenciesTxt() throws IOException {
        Set<String> provided = new HashSet<>();

        for (ArtifactSpec each : this.dependencies) {
            if (each.packaging.equals("jar")) {
                try (JarFile jar = new JarFile(each.file)) {

                    ZipEntry entry = jar.getEntry("provided-dependencies.txt");
                    if (entry != null) {
                        // add ourselves
                        provided.add(each.groupId + ":" + each.artifactId);

                        try (InputStream in = jar.getInputStream(entry)) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            String line = null;

                            // add everything mentioned in the file
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                if (line.length() > 0) {
                                    provided.add(line);
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
                            provided.add(line);
                        }
                    }
                }
            }
        }

        StringBuilder depsTxt = new StringBuilder();

        for (ArtifactSpec each : this.dependencies) {
            if (provided.contains(each.groupId + ":" + each.artifactId)) {
                continue;
            }
            if (each.scope.equals("compile") && each.packaging.equals("jar")) {
                //this.dependencies.add(each.groupId + ":" + each.artifactId + ":" + each.version);
                each.shouldGather = true;
                depsTxt.append(each.groupId).append(':').append(each.artifactId).append(':').append(each.version).append("\n");
            }
        }

        this.archive.addAsManifestResource( new StringAsset( depsTxt.toString() ), "wildfly-swarm-dependencies.txt" );
    }

    protected void collectDependencies() throws Exception {
        if (!this.bundleDependencies) {
            return;
        }
        analyzeModuleDependencies();
        gatherDependencies();
    }


    protected void analyzeModuleDependencies() throws IOException {
        for (ArtifactSpec each : this.dependencies) {
            if (includeAsBootstrapJar(each)) {
                analyzeModuleDependencies(each);
            }
        }
    }

    private static final Pattern ARTIFACT_PATTERN = Pattern.compile("<artifact name=\"([^\"]+)\".*");

    protected void analyzeModuleDependencies(ArtifactSpec artifact) throws IOException {
        if (!artifact.packaging.equals("jar")) {
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(moduleXml));

        String line = null;

        while ((line = reader.readLine()) != null) {
            Matcher matcher = ARTIFACT_PATTERN.matcher(line.trim());
            if (matcher.matches()) {
                String parts[] = matcher.group(1).split(":");
                String groupId = parts[0];
                String artifactId = parts[1];
                String version = parts[2];
                String packaging = "jar";
                String classifier = null;
                if (parts.length > 3) {
                    classifier = parts[3];
                }
                //this.dependencies.add(matcher.group(1));
                ArtifactSpec dep = new ArtifactSpec("compile", groupId, artifactId, version, packaging, classifier, null);
                dep.shouldGather = true;
                this.moduleDependencies.add(dep);
            }
        }

    }

    protected void gatherDependencies() throws Exception {
        this.dependencies.addAll(this.moduleDependencies);
        for (ArtifactSpec each : this.dependencies) {
            if (each.shouldGather) {
                gatherDependency(each);
            }
        }
    }

    private File createJar(String baseName, Path dir) throws IOException {
        File out = new File( dir.toFile(), baseName + "-swarm.jar" );
        ZipExporter exporter = this.archive.as(ZipExporter.class);
        exporter.exportTo(out, true);
        return out;
    }

    public ArtifactSpec findArtifact(String groupId, String artifactId, String version, String packaging, String classifier) {
        for (ArtifactSpec each : this.dependencies) {
            if (groupId != null && !groupId.equals(each.groupId)) {
                continue;
            }

            if (artifactId != null && !artifactId.equals(each.artifactId)) {
                continue;
            }

            if (version != null && !version.equals(each.version)) {
                continue;
            }

            if (packaging != null && !packaging.equals(each.packaging)) {
                continue;
            }

            if (classifier != null && !classifier.equals(each.classifier)) {
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
            if ( each.getName().startsWith( "org/jboss/modules/ModuleLoader" ) ) {
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
            if ( each.isDirectory() ) {
                continue;
            }
            this.archive.add(new ZipFileEntryAsset(jarFile, each), each.getName());
        }
    }
}
