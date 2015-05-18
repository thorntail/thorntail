package org.wildfly.swarm.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

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
public class PackageMojo extends AbstractSwarmMojo {

    @Parameter(alias = "modules")
    private String[] additionalModules;

    @Parameter(alias = "bundle-dependencies", defaultValue = "true")
    private boolean bundleDependencies;

    @Parameter(alias="mainClass")
    private String mainClass;

    private Path dir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setupDirectory();
        addWildflySwarmBootstrapJar();
        addBootstrapJars();
        createManifest();
        createProperties();
        createJar();
    }

    private void setupDirectory() throws MojoFailureException {
        this.dir = Paths.get(this.projectBuildDir, "wildfly-swarm-archive");
        try {
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
            } else {
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
                System.err.println( "test: " + each );
                if (includeAsBootstrapJar(each)) {
                    Path fs = bootstrapJars.resolve( each.getArtifactId() + "-" + each.getVersion() + "." + each.getType() );
                    Files.copy( each.getFile().toPath(), fs );
                }
            }
            Files.copy(this.project.getArtifact().getFile().toPath(), bootstrapJars.resolve(this.project.getArtifactId() + "-" + this.project.getVersion() + "." + this.project.getPackaging()));
        } catch (IOException e) {
            throw new MojoFailureException("Unable to create _bootstrap directory", e);
        }
    }

    private boolean includeAsBootstrapJar(Artifact artifact) {
        // TODO figure out a better more generic way
        /*
        if (artifact.getGroupId().equals("org.wildfly.swarm")) {
            System.err.println( "is swarm" );
            return true;
        }

        if ( artifact.equals( this.project.getArtifact() ) ) {
            System.err.println( "is project" );
            return true;
        }
        */

        if ( artifact.getGroupId().equals( "org.wildfly.swarm" ) && artifact.getArtifactId().equals( "wildfly-swarm-bootstrap" ) ) {
            return false;
        }

        return ! artifact.getScope().equals( "provided" );

        //return false;
    }


    private void createManifest() throws MojoFailureException {
        Manifest manifest = new Manifest();

        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, "org.wildfly.swarm.bootstrap.Main");
        if ( this.mainClass != null && ! this.mainClass.equals( "" ) ) {
            attrs.put(new Attributes.Name("Wildfly-Swarm-Main-Class"), this.mainClass );
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

    private void createProperties() throws MojoFailureException {

        Path propsPath = dir.resolve("META-INF").resolve("wildfly-swarm.properties");

        Properties props = new Properties();

        try {
            try ( FileOutputStream out = new FileOutputStream( propsPath.toFile() ) ) {
                props.store( out, "Generated By Wildfly Swarm" );
            }
        } catch (IOException e) {
            throw new MojoFailureException( "Unable to create META-INF/wildfly-swarm.properties", e );
        }

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


        String name = artifact.getArtifactId() + "-" + artifact.getVersion() + "-swarm.jar";

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










    private void addMavenRepository() throws MojoFailureException {
        if (!this.bundleDependencies) {
            return;
        }
        Path modulesDir = this.dir.resolve("modules");

        //analyzeModuleXmls(modulesDir);
        collectArtifacts();
    }

    private void collectArtifacts() throws MojoFailureException {
        for (ArtifactSpec each : this.gavs) {
            if (!collectArtifact(each)) {
                getLog().error("unable to locate artifact: " + each);
            }
        }
    }

    private boolean collectArtifact(ArtifactSpec spec) throws MojoFailureException {
        Artifact artifact = locateArtifact(spec);
        if (artifact == null) {
            return false;
        }

        addArtifact(artifact);
        return true;
    }

    private void addArtifact(Artifact artifact) throws MojoFailureException {
        Path m2repo = this.dir.resolve("m2repo");
        Path dest = m2repo.resolve(ArtifactUtils.toPath(artifact));

        try {
            Files.createDirectories(dest.getParent());
            Files.copy(artifact.getFile().toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MojoFailureException("unable to add artifact: " + dest, e);
        }
    }

    private Artifact locateArtifact(ArtifactSpec spec) {
        for (Artifact each : this.featurePackArtifacts) {
            if (spec.matches(each)) {
                return each;
            }
        }

        for (Artifact each : this.pluginArtifacts) {
            if (spec.matches(each)) {
                return each;
            }
        }

        for (Artifact each : this.project.getArtifacts()) {
            if (spec.matches(each)) {
                return each;
            }
        }

        return null;
    }


    private void emptyDir(Path dir) throws IOException {
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


    private void addProjectArtifact() throws MojoFailureException {
        Artifact artifact = this.project.getArtifact();

        Path appDir = this.dir.resolve("app");
        Path appFile = appDir.resolve(artifact.getFile().getName());

        try {
            Files.createDirectories(appDir);
            Files.copy(artifact.getFile().toPath(), appFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MojoFailureException("Error copying project artifact", e);
        }
    }

    private void addProjectDependenciesToRepository() throws MojoFailureException {
        if (!this.bundleDependencies) {
            return;
        }

        if (!this.project.getPackaging().equals("jar")) {
            return;
        }

        Path depsTxt = this.dir.resolve("dependencies.txt");
        try (final BufferedWriter out = Files.newBufferedWriter(depsTxt, StandardCharsets.UTF_8)) {
            Set<Artifact> dependencies = this.project.getArtifacts();

            for (Artifact each : dependencies) {
                String scope = each.getScope();
                if (scope.equals("compile") || scope.equals("runtime")) {
                    addArtifact(each);
                    out.write(each.getGroupId() + ":" + each.getArtifactId() + ":" + each.getVersion() + "\n");
                }
            }
        } catch (IOException e) {
            throw new MojoFailureException("Unable to create dependencies.txt", e);
        }

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


    /*

    */

}
