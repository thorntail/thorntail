package org.wildfly.swarm.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(
        name = "package",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class PackageMojo extends AbstractMojo { //extends AbstractSwarmMojo {

    private static final Pattern ARTIFACT_PATTERN = Pattern.compile("<artifact name=\"([^\"]+)\".*");

    @Component
    protected MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${repositorySystemSession}")
    protected DefaultRepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}")
    protected List<ArtifactRepository> remoteRepositories;

    @Parameter(alias = "modules")
    private String[] additionalModules;

    @Parameter(alias = "bundleDependencies", defaultValue = "true")
    private boolean bundleDependencies;

    @Parameter(alias = "mainClass")
    private String mainClass;

    @Inject
    private ArtifactResolver resolver;

    private Path dir;

    private Set<String> dependencies = new HashSet<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setupDirectory();
        addWildflySwarmBootstrapJar();
        addBootstrapJars();
        createManifest();
        createWildflySwarmProperties();
        createDependenciesTxt();
        collectDependencies();
        createJar();
    }

    private void setupDirectory() throws MojoFailureException {
        this.dir = Paths.get(this.projectBuildDir, "wildfly-swarm-archive");
        try {
            if (Files.exists(dir)) {
                emptyDir(dir);
            }
        } catch (IOException e) {
            throw new MojoFailureException("Failed to setup wildfly-swarm-archive directory", e);
        }
    }

    private void addWildflySwarmBootstrapJar() throws MojoFailureException {
        Artifact artifact = findArtifact("org.wildfly.swarm", "wildfly-swarm-bootstrap", "jar");
        try {
            expandArtifact(artifact);
        } catch (IOException e) {
            throw new MojoFailureException("Unable to add bootstrap", e);
        }
    }

    private void addBootstrapJars() throws MojoFailureException {

        try {
            Path bootstrapJars = this.dir.resolve("_bootstrap");
            Files.createDirectories(bootstrapJars);

            Set<Artifact> artifacts = this.project.getArtifacts();

            for (Artifact each : artifacts) {
                if (includeAsBootstrapJar(each)) {
                    Path fs = bootstrapJars.resolve(each.getArtifactId() + "-" + each.getVersion() + "." + each.getType());
                    Files.copy(each.getFile().toPath(), fs);
                }
            }
            Files.copy(this.project.getArtifact().getFile().toPath(), bootstrapJars.resolve(this.project.getArtifactId() + "-" + this.project.getVersion() + "." + this.project.getPackaging()));
        } catch (IOException e) {
            throw new MojoFailureException("Unable to create _bootstrap directory", e);
        }
    }

    private boolean includeAsBootstrapJar(Artifact artifact) {
        // TODO figure out a better more generic way
        if (artifact.getGroupId().equals("org.wildfly.swarm") && artifact.getArtifactId().equals("wildfly-swarm-bootstrap")) {
            return false;
        }

        if (artifact.getGroupId().equals("org.wildfly.swarm")) {
            return true;
        }

        if (artifact.getGroupId().equals("org.jboss.shrinkwrap")) {
            return true;
        }

        if (artifact.getGroupId().equals("org.jboss.msc") && artifact.getArtifactId().equals("jboss-msc")) {
            return false;
        }

        return !artifact.getScope().equals("provided");
    }

    private void createManifest() throws MojoFailureException {
        Manifest manifest = new Manifest();

        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, "org.wildfly.swarm.bootstrap.Main");
        if (this.mainClass != null && !this.mainClass.equals("")) {
            attrs.put(new Attributes.Name("Wildfly-Swarm-Main-Class"), this.mainClass);
        }
        attrs.putValue("Application-Artifact", this.project.getArtifact().getFile().getName());

        // Write the manifest to the dir
        final Path manifestPath = dir.resolve("META-INF").resolve("MANIFEST.MF");
        // Ensure the directories have been created
        try {
            Files.createDirectories(manifestPath.getParent());
            try (final OutputStream out = Files.newOutputStream(manifestPath, StandardOpenOption.CREATE)) {
                manifest.write(out);
            }
        } catch (IOException e) {
            throw new MojoFailureException("Could not create manifest file: " + manifestPath.toString(), e);
        }
    }

    private void createWildflySwarmProperties() throws MojoFailureException {

        Path propsPath = dir.resolve("META-INF").resolve("wildfly-swarm.properties");

        Properties props = new Properties();
        props.setProperty("wildfly.swarm.app.artifact", this.project.getBuild().getFinalName() + "." + this.project.getPackaging());

        try {
            try (FileOutputStream out = new FileOutputStream(propsPath.toFile())) {
                props.store(out, "Generated By Wildfly Swarm");
            }
        } catch (IOException e) {
            throw new MojoFailureException("Unable to create META-INF/wildfly-swarm.properties", e);
        }
    }

    private void createDependenciesTxt() throws MojoFailureException {
        Set<String> provided = new HashSet<>();
        Set<Artifact> artifacts = this.project.getArtifacts();

        for (Artifact each : artifacts) {
            if (each.getType().equals("jar")) {
                try {
                    try (JarFile jar = new JarFile(each.getFile())) {

                        ZipEntry entry = jar.getEntry("provided-dependencies.txt");
                        if (entry != null) {
                            // add ourselves
                            provided.add(each.getGroupId() + ":" + each.getArtifactId());

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
                } catch (IOException e) {
                    throw new MojoFailureException("Unable to inspect jar", e);
                }
            }
        }

        List<Resource> resources = this.project.getResources();
        for (Resource each : resources) {
            Path providedDependencies = Paths.get(each.getDirectory(), "provided-dependencies.txt");
            if (Files.exists(providedDependencies)) {

                try {
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
                } catch (IOException e) {
                    throw new MojoFailureException("Error reading project's provided-dependencies.txt");
                }
            }
        }

        Path depsPath = dir.resolve("META-INF").resolve("wildfly-swarm-dependencies.txt");

        try (FileWriter out = new FileWriter(depsPath.toFile())) {
            for (Artifact each : artifacts) {
                if (provided.contains(each.getGroupId() + ":" + each.getArtifactId())) {
                    continue;
                }
                if (each.getScope().equals("compile")) {
                    this.dependencies.add(each.getGroupId() + ":" + each.getArtifactId() + ":" + each.getVersion());
                    out.write(each.getGroupId() + ":" + each.getArtifactId() + ":" + each.getVersion() + "\n");
                }

            }
        } catch (Exception e) {
            throw new MojoFailureException("Unable to create META-INF/wildfly-swarm-dependencies.txt");
        }
    }

    protected void collectDependencies() throws MojoFailureException {
        if (!this.bundleDependencies) {
            return;
        }
        try {
            analyzeModuleDependencies();
        } catch (IOException e) {
            throw new MojoFailureException("Unable to collect dependencies", e);
        }

        gatherDependencies();
    }

    protected void analyzeModuleDependencies() throws IOException {
        for (Artifact each : this.project.getArtifacts()) {
            if (includeAsBootstrapJar(each)) {
                analyzeModuleDependencies(each);
            }
        }
    }

    protected void analyzeModuleDependencies(Artifact artifact) throws IOException {

        JarFile jar = new JarFile(artifact.getFile());

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
                this.dependencies.add(matcher.group(1));
            }
        }

    }

    protected void gatherDependencies() throws MojoFailureException {
        for (String each : this.dependencies) {
            try {
                gatherDependency(each);
            } catch (ArtifactResolutionException e) {
                throw new MojoFailureException("Unable to resolve artifact: " + each, e);
            }
        }
    }

    protected void gatherDependency(String gav) throws ArtifactResolutionException, MojoFailureException {
        //System.err.println( "gather: " + gav );
        String[] parts = gav.split(":");

        if (parts.length < 3) {
            throw new MojoFailureException("GAV must be at least 3 parts: " + gav);
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String packaging = "jar";
        String version = null;
        String classifier = null;

        if (parts.length > 3) {
            version = parts[2];
            classifier = parts[3];
        } else {
            version = parts[2];
        }

        /*
        System.err.println( "groupId: " + groupId );
        System.err.println( "artifactId: " + artifactId );
        System.err.println( "version: " + version );
        System.err.println( "packaging: " + packaging );
        System.err.println( "classifier: " + classifier );
        */

        ArtifactRequest request = new ArtifactRequest();

        org.eclipse.aether.artifact.DefaultArtifact aetherArtifact
                = new org.eclipse.aether.artifact.DefaultArtifact(groupId, artifactId, classifier, packaging, version);

        request.setArtifact(aetherArtifact);
        request.setRepositories(remoteRepositories());

        ArtifactResult result = resolver.resolveArtifact(this.repositorySystemSession, request);

        if (result.isResolved()) {
            try {
                gatherDependency(result.getArtifact());
            } catch (IOException e) {
                throw new MojoFailureException("Unable to gather dependenc: " + gav, e);
            }
        }
    }

    protected void gatherDependency(org.eclipse.aether.artifact.Artifact artifact) throws IOException {
        Path artifactPath = this.dir.resolve("m2repo");

        String[] groupIdParts = artifact.getGroupId().split("\\.");

        for (int i = 0; i < groupIdParts.length; ++i) {
            artifactPath = artifactPath.resolve(groupIdParts[i]);
        }

        artifactPath = artifactPath.resolve(artifact.getArtifactId());
        artifactPath = artifactPath.resolve(artifact.getVersion());
        artifactPath = artifactPath.resolve(artifact.getFile().getName());

        Files.createDirectories(artifactPath.getParent());
        Files.copy(artifact.getFile().toPath(), artifactPath);
    }

    private void createJar() throws MojoFailureException {

        Artifact primaryArtifact = this.project.getArtifact();

        ArtifactHandler handler = new DefaultArtifactHandler("jar");
        Artifact artifact = new DefaultArtifact(
                primaryArtifact.getGroupId(),
                primaryArtifact.getArtifactId(),
                primaryArtifact.getVersion(),
                primaryArtifact.getScope(),
                "jar",
                "swarm",
                handler
        );

        String name = this.project.getBuild().getFinalName() + "-swarm.jar";

        File file = new File(this.projectBuildDir, name);
        try (
                FileOutputStream fileOut = new FileOutputStream(file);
                JarOutputStream out = new JarOutputStream(fileOut)
        ) {
            writeToJar(out, this.dir);
        } catch (IOException e) {
            throw new MojoFailureException("Unable to create jar", e);
        }

        artifact.setFile(file);

        this.project.addAttachedArtifact(artifact);
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

    private void expandArtifact(Artifact artifact) throws IOException {

        Path destination = this.dir;

        File artifactFile = artifact.getFile();

        if (artifact.getType().equals("jar")) {
            JarFile jarFile = new JarFile(artifactFile);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry each = entries.nextElement();
                if (each.getName().startsWith("META-INF")) {
                    continue;
                }
                Path fsEach = destination.resolve(each.getName());
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
    }

    private Artifact findArtifact(String groupId, String artifactId, String type) {
        Set<Artifact> artifacts = this.project.getArtifacts();

        for (Artifact each : artifacts) {
            if (each.getGroupId().equals(groupId) && each.getArtifactId().equals(artifactId) && each.getType().equals(type)) {
                return each;
            }
        }

        return null;
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

    protected List<RemoteRepository> remoteRepositories() {
        List<RemoteRepository> repos = new ArrayList<>();

        for (ArtifactRepository each : this.remoteRepositories) {
            RemoteRepository.Builder builder = new RemoteRepository.Builder(each.getId(), "default", each.getUrl());
            repos.add(builder.build());
        }

        repos.add(new RemoteRepository.Builder("jboss-public-repository-group", "default", "http://repository.jboss.org/nexus/content/groups/public/").build());

        return repos;
    }

}
