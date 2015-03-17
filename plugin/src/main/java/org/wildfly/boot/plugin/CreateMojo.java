package org.wildfly.boot.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
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

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Bob McWhirter
 */
@Mojo(
        name = "create",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE,
        requiresDependencyResolution = ResolutionScope.COMPILE
)
public class CreateMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    private String projectBuildDir;


    @Parameter(defaultValue = "${repositorySystemSession}")
    private DefaultRepositorySystemSession session;

    @Parameter(defaultValue="${project.remoteArtifactRepositories}")
    private List<ArtifactRepository> remoteRepositories;

    @Inject
    private ArtifactResolver resolver;

    private Set<String> featurePackModules = new HashSet<>();

    private File dir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setupDirectory();
        addJBossModules();
        addBootstrap();
        addFeaturePacks();
        //addModules();
        //addMavenRepository();
        addProjectArtifact();
        createJar();
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
                "boot",
                handler
        );


        String name = artifact.getArtifactId() + "-" + artifact.getVersion() + "-boot.jar";

        File file = new File(this.projectBuildDir, name);

        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            Manifest manifest = createManifest();
            JarOutputStream out = new JarOutputStream(fileOut, manifest);

            try {
                writeToJar(out, this.dir);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new MojoFailureException("Unable to create jar", e);
        }

        artifact.setFile(file);

        this.project.addAttachedArtifact(artifact);
    }

    private void writeToJar(JarOutputStream out, File entry) throws IOException {
        String rootPath = this.dir.getAbsolutePath();
        String entryPath = entry.getAbsolutePath();


        if (!rootPath.equals(entryPath)) {
            String jarPath = entryPath.substring(rootPath.length() + 1);
            if (entry.isDirectory()) {
                jarPath = jarPath + "/";
            }
            out.putNextEntry(new ZipEntry(jarPath));
        }

        if (entry.isDirectory()) {
            File[] children = entry.listFiles();
            for (int i = 0; i < children.length; ++i) {
                writeToJar(out, children[i]);
            }
        } else {
            FileInputStream in = new FileInputStream(entry);
            try {
                byte[] buf = new byte[1024];
                int len = -1;

                while ((len = in.read(buf)) >= 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                in.close();
            }
        }
    }

    private void addJBossModules() throws MojoFailureException {
        Artifact artifact = findArtifact("org.jboss.modules", "jboss-modules", "jar");
        try {
            addArtifact(artifact, "", true);
        } catch (IOException e) {
            throw new MojoFailureException("Unable to add jboss-modules");
        }
    }

    private void addBootstrap() throws MojoFailureException {
        Artifact artifact = findArtifact("org.wildfly.boot", "wildfly-boot-bootstrap", "jar");
        try {
            addArtifact(artifact, "", true);
        } catch (IOException e) {
            throw new MojoFailureException("Unable to add bootstrap");
        }
    }

    private List<RemoteRepository> remoteRepositories() {
        List<RemoteRepository> repos = new ArrayList<>();

        for ( ArtifactRepository each : this.remoteRepositories ) {
            RemoteRepository.Builder builder = new RemoteRepository.Builder( each.getId(), "default", each.getUrl() );
            repos.add( builder.build() );
        }

        return repos;
    }

    private void addFeaturePacks() throws MojoFailureException {
        Set<Artifact> artifacts = this.project.getArtifacts();

        List<org.eclipse.aether.artifact.Artifact> packs = new ArrayList<>();

        for ( Artifact each : artifacts ) {
            ArtifactRequest request = new ArtifactRequest();
            org.eclipse.aether.artifact.DefaultArtifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(each.getGroupId(), each.getArtifactId(), "feature-pack", "zip", each.getVersion());
            request.setArtifact(artifact);
            request.setRepositories( remoteRepositories() );
            try {
                ArtifactResult result = resolver.resolveArtifact(this.session, request);
                packs.add(result.getArtifact());
            } catch (ArtifactResolutionException e) {
                // skip
            }
        }

        for ( org.eclipse.aether.artifact.Artifact each : packs ) {
            try {
                expandFeaturePack(each);
            } catch (IOException e) {
                throw new MojoFailureException("error expanding feature-pack", e );
            }
        }
    }

    private void expandFeaturePack(org.eclipse.aether.artifact.Artifact artifact) throws IOException {

        ZipFile zipFile = new ZipFile(artifact.getFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry each = entries.nextElement();

            File fsEach = new File(this.dir, each.getName());

            if ( each.getName().equals("feature-pack.txt") ) {
                recordFeaturePackModuleName(zipFile, each);
                continue;
            }

            if ( each.isDirectory() ) {
                fsEach.mkdirs();
                continue;
            }

            FileOutputStream out = new FileOutputStream(fsEach);
            try {
                InputStream in = zipFile.getInputStream(each);
                try {

                    byte[] buf = new byte[1024];
                    int len = -1;

                    while ((len = in.read(buf)) >= 0) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    in.close();
                }
            } finally {
                out.close();
            }
        }
    }

    private void recordFeaturePackModuleName(ZipFile zipFile, ZipEntry entry) throws IOException {
        BufferedReader in = new BufferedReader( new InputStreamReader( zipFile.getInputStream(entry) ) );

        try {

            String line = null;

            while ( ( line = in.readLine() ) != null ) {
                line = line.trim();
                if ( line.equals( "" ) || line.startsWith( "//" ) ) {
                    continue;
                }
                this.featurePackModules.add(line);
            }
        } finally {
            in.close();
        }

    }

    private void addProjectArtifact() throws MojoFailureException {
        Artifact artifact = this.project.getArtifact();

        File appDir = new File(this.dir, "app");
        File appFile = new File(appDir, artifact.getFile().getName());

        appDir.mkdirs();

        try {
            FileInputStream in = new FileInputStream(artifact.getFile());

            try {
                FileOutputStream out = new FileOutputStream(appFile);
                try {
                    byte[] buf = new byte[1024];
                    int len = -1;

                    while ((len = in.read(buf)) >= 0) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new MojoFailureException("Error copying project artifact", e);

        }
    }

    private Manifest createManifest() throws MojoFailureException {
        Manifest manifest = new Manifest();

        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, "org.wildfly.boot.bootstrap.Main");
        attrs.putValue("Application-Artifact", this.project.getArtifact().getFile().getName());

        StringBuilder modules = new StringBuilder();
        boolean first = true;

        for ( String each : this.featurePackModules ) {
            if ( ! first ) {
                modules.append( "," );
            }

            modules.append( each );
            first = false;
        }
        attrs.putValue("Feature-Pack-Modules", modules.toString() );

        return manifest;
    }

    private String createPath(Artifact artifact) {

        String path = artifact.getGroupId().replaceAll("\\.", File.separator);

        path = path + File.separator + artifact.getArtifactId();
        path = path + File.separator + artifact.getVersion();
        path = path + File.separator + artifact.getFile().getName();

        return path;
    }

    private void addArtifact(Artifact artifact, String prefix, boolean expand) throws IOException {

        File destination = new File(this.dir, prefix);

        File artifactFile = artifact.getFile();

        if (expand) {
            if (artifact.getType().equals("jar")) {
                JarFile jarFile = new JarFile(artifactFile);
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry each = entries.nextElement();
                    if (each.getName().startsWith("META-INF")) {
                        continue;
                    }
                    File fsEach = new File(destination, each.getName());
                    if (each.isDirectory()) {
                        fsEach.mkdir();
                    } else {
                        FileOutputStream out = new FileOutputStream(fsEach);
                        try {
                            InputStream in = jarFile.getInputStream(each);
                            try {

                                byte[] buf = new byte[1024];
                                int len = -1;

                                while ((len = in.read(buf)) >= 0) {
                                    out.write(buf, 0, len);
                                }
                            } finally {
                                in.close();
                            }
                        } finally {
                            out.close();
                        }
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


    private void setupDirectory() {
        this.dir = new File(this.projectBuildDir, "wildfly-boot-archive");
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            emptyDir(dir);
        }
    }

    private void emptyDir(File dir) {
        File[] children = dir.listFiles();

        for (int i = 0; i < children.length; ++i) {
            deleteFile(children[i]);
        }
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            emptyDir(file);
        }

        file.delete();
    }
}
