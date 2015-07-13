package org.wildfly.swarm.tools;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * @author Bob McWhirter
 */
public class BuildTool {

    private String mainClass;
    private String contextPath = "/";
    //private int httpPort = 8080;
    //private int portOffset = 0;
    //private String bindAddress = "0.0.0.0";
    private boolean bundleDependencies = true;

    private final Set<ArtifactSpec> dependencies = new HashSet<>();
    private final Set<ArtifactSpec> moduleDependencies = new HashSet<>();
    private final Set<String> resourceDirectories = new HashSet<>();
    private ArtifactSpec projectArtifact;
    private ArtifactResolvingHelper resolver;

    private Path outputDir;
    private final Path dir;
    private Properties properties = new Properties();

    public BuildTool(File outputDir) {
        this(outputDir.toPath());
    }

    public BuildTool(Path outputDir) {
        this.outputDir = outputDir;
        this.dir = this.outputDir.resolve("wildfly-swarm-archive");
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
        this.properties.putAll( properties );
        return this;
    }

    public BuildTool bundleDependencies(boolean bundleDependencies) {
        this.bundleDependencies = bundleDependencies;
        return this;
    }

    public BuildTool projectArtifact(String groupId, String artifactId, String version, String packaging, File file) {
        this.projectArtifact = new ArtifactSpec(null, groupId, artifactId, version, packaging, null, file);
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

    public File build(String baseName) throws Exception {
        return build( baseName, this.outputDir );
    }

    public File build(String baseName, Path dir) throws Exception {
        prepareDir();
        addWildflySwarmBootstrapJar();
        addBootstrapJars();
        createManifest();
        createWildflySwarmProperties();
        createDependenciesTxt();
        collectDependencies();
        return createJar(baseName, dir);
    }

    private void prepareDir() throws IOException {
        if (Files.exists(this.dir)) {
            emptyDir(this.dir);
        }
    }

    private void addWildflySwarmBootstrapJar() throws BuildException, IOException {
        ArtifactSpec artifact = findArtifact("org.wildfly.swarm", "wildfly-swarm-bootstrap", null, "jar", null);
        if (artifact == null) {
            throw new BuildException("Unable to locate wildfly-swarm-bootstrap.jar in project dependencies.");
        }
        expandArtifact(artifact.file);
    }


    private void addBootstrapJars() throws Exception {

        Set<String> bootstrapGavs = new HashSet<>();
        Path projectArtifactPath = null;

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

        Path bootstrapJars = this.dir.resolve("_bootstrap");
        projectArtifactPath = bootstrapJars.resolve(this.projectArtifact.artifactId + "-" + this.projectArtifact.version + "." + this.projectArtifact.packaging);
        Files.createDirectories(bootstrapJars);
        Files.copy(this.projectArtifact.file.toPath(), projectArtifactPath);


        final Path bootstrapTxt = dir.resolve("META-INF").resolve("wildfly-swarm-bootstrap.txt");
        Files.createDirectories(bootstrapTxt.getParent());
        try (final OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(bootstrapTxt, StandardOpenOption.CREATE))) {
            for (String each : bootstrapGavs) {
                out.write("gav: " + each + "\n");
            }
            out.write("path: _bootstrap/" + this.projectArtifact.artifactId + "-" + this.projectArtifact.version + "." + this.projectArtifact.packaging + "\n");
        }
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

        return !dependency.scope.equals("provided");
    }

    protected void gatherDependency(ArtifactSpec artifact) throws Exception {
        ArtifactSpec originalArtifact = artifact;
        if ( artifact.file == null ) {
            artifact = this.resolver.resolve(artifact);
        }

        if (artifact == null) {
            throw new BuildException("Unable to resolve artifact: " + originalArtifact);
        }

        Path artifactPath = this.dir.resolve("m2repo");

        String[] groupIdParts = artifact.groupId.split("\\.");

        for (int i = 0; i < groupIdParts.length; ++i) {
            artifactPath = artifactPath.resolve(groupIdParts[i]);
        }

        artifactPath = artifactPath.resolve(artifact.artifactId);
        artifactPath = artifactPath.resolve(artifact.version);
        artifactPath = artifactPath.resolve(artifact.file.getName());

        if (Files.exists(artifactPath)) {
            return;
        }

        Files.createDirectories(artifactPath.getParent());
        Files.copy(artifact.file.toPath(), artifactPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void createManifest() throws IOException {
        Manifest manifest = new Manifest();

        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, "org.wildfly.swarm.bootstrap.Main");
        if (this.mainClass != null && !this.mainClass.equals("")) {
            attrs.put(new Attributes.Name("Wildfly-Swarm-Main-Class"), this.mainClass);
        }
        attrs.putValue("Application-Artifact", this.projectArtifact.file.getName());

        // Write the manifest to the dir
        final Path manifestPath = dir.resolve("META-INF").resolve("MANIFEST.MF");
        // Ensure the directories have been created
        Files.createDirectories(manifestPath.getParent());
        try (final OutputStream out = Files.newOutputStream(manifestPath, StandardOpenOption.CREATE)) {
            manifest.write(out);
        }
    }

    private void createWildflySwarmProperties() throws IOException {
        Path propsPath = dir.resolve("META-INF").resolve("wildfly-swarm.properties");

        Properties props = new Properties();

        Enumeration<?> propNames = this.properties.propertyNames();

        while ( propNames.hasMoreElements() ) {
            String eachName = (String) propNames.nextElement();
            String eachValue = this.properties.get(eachName).toString();
            props.put(eachName, eachValue);
        }
        //props.putAll( this.properties );

        props.setProperty("wildfly.swarm.app.artifact", this.projectArtifact.artifactId + "-" + this.projectArtifact.version + "." + this.projectArtifact.packaging);
        props.setProperty("wildfly.swarm.context.path", this.contextPath);

        try (FileOutputStream out = new FileOutputStream(propsPath.toFile())) {
            props.store(out, "Generated By Wildfly Swarm");
        }
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

        Path depsPath = dir.resolve("META-INF").resolve("wildfly-swarm-dependencies.txt");

        try (FileWriter out = new FileWriter(depsPath.toFile())) {
            for (ArtifactSpec each : this.dependencies) {
                if (provided.contains(each.groupId + ":" + each.artifactId)) {
                    continue;
                }
                if (each.scope.equals("compile") && each.packaging.equals("jar")) {
                    //this.dependencies.add(each.groupId + ":" + each.artifactId + ":" + each.version);
                    each.shouldGather = true;
                    out.write(each.groupId + ":" + each.artifactId + ":" + each.version + "\n");
                }

            }
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

        File file = dir.resolve(baseName + "-swarm.jar").toFile();
        try (
                FileOutputStream fileOut = new FileOutputStream(file);
                JarOutputStream out = new JarOutputStream(fileOut)
        ) {
            writeToJar(out, this.dir);
        }

        return file;
    }

    private void writeToJar(final JarOutputStream out, final Path entry) throws IOException {
        String rootPath = this.dir.toAbsolutePath().toString();
        String entryPath = entry.toAbsolutePath().toString();

        if (!rootPath.equals(entryPath)) {
            String jarPath = entryPath.substring(rootPath.length() + 1);
            if (Files.isDirectory(entry)) {
                jarPath = jarPath + "/";
            }
            out.putNextEntry(new ZipEntry(jarPath.replace(File.separatorChar, '/')));
        }

        if (Files.isDirectory(entry)) {
            Files.walkFileTree(entry, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    writeToJar(out, file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.copy(entry, out);
        }
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

    public void expandArtifact(File artifactFile) throws IOException {

        JarFile jarFile = new JarFile(artifactFile);
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry each = entries.nextElement();
            if (each.getName().startsWith("META-INF")) {
                continue;
            }
            Path fsEach = this.dir.resolve(each.getName());
            if (each.isDirectory()) {
                Files.createDirectories(fsEach);
            } else {
                try (InputStream in = jarFile.getInputStream(each)) {
                    Files.createDirectories(fsEach.getParent());
                    Files.copy(in, fsEach, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void emptyDir(final Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
