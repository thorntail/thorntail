package org.wildfly.selfcontained.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
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
public class SelfContainedMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    private String projectBuildDir;

    private File dir;
    private Set<Artifact> excludedArtifacts = new HashSet<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setupDirectory();
        addJBossModules();
        addBootstrap();
        addMavenRepository();
        addModules();
        addProjectArtifact();
        createJar();
    }

    private void createJar() throws MojoFailureException {

        Artifact primaryArtifact = this.project.getArtifact();

        Artifact artifact = new DefaultArtifact(
                primaryArtifact.getGroupId(),
                primaryArtifact.getArtifactId(),
                primaryArtifact.getVersion(),
                primaryArtifact.getScope(),
                "jar",
                "self-contained",
                primaryArtifact.getArtifactHandler()
        );

        String name = artifact.getArtifactId() + "-" + artifact.getVersion() + "-self-contained.jar";

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

        this.excludedArtifacts.add(artifact);
    }

    private void addBootstrap() throws MojoFailureException {
        Artifact artifact = findArtifact("org.wildfly.self-contained", "bootstrap", "jar");
        try {
            addArtifact(artifact, "", true);
        } catch (IOException e) {
            throw new MojoFailureException("Unable to add bootstrap");
        }

        this.excludedArtifacts.add(artifact);
    }

    private void addMavenRepository() throws MojoFailureException {

        Set<Artifact> artifacts = this.project.getArtifacts();

        for (Artifact each : artifacts) {
            if (each.getScope() == "provided") {
                if (this.excludedArtifacts.contains(each)) {
                    continue;
                }
                try {
                    addMavenRepository(each);
                } catch (IOException e) {
                    throw new MojoFailureException("error copying " + each);
                }
            }
        }
    }

    private void addMavenRepository(Artifact artifact) throws IOException {
        File m2Repo = new File(this.dir, "m2repo");

        String path = createPath(artifact);
        File dest = new File(m2Repo, path);
        dest.getParentFile().mkdirs();

        FileInputStream in = new FileInputStream(artifact.getFile());
        try {
            FileOutputStream out = new FileOutputStream(dest);

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
    }

    private void addModules() throws MojoFailureException {

        Set<Artifact> artifacts = this.project.getArtifacts();

        for (Artifact each : artifacts) {
            if (each.getType().equals("zip") && each.getScope().equals("provided")) {
                try {
                    addModules(each);
                } catch (IOException e) {
                    throw new MojoFailureException("unable to extract modules from " + each, e);
                }
            }
        }
    }

    private void addModules(Artifact artifact) throws IOException {

        ZipFile zipFile = new ZipFile(artifact.getFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry each = entries.nextElement();

            if (!each.getName().startsWith("modules/")) {
                continue;
            }

            File fsEach = new File(this.dir, each.getName());

            if (each.isDirectory()) {
                fsEach.mkdir();
            } else {
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
        attrs.put(Attributes.Name.MAIN_CLASS, "org.wildfly.selfcontained.bootstrap.Main");
        attrs.putValue("Application-Artifact", this.project.getArtifact().getFile().getName());

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
        this.dir = new File(this.projectBuildDir, "self-contained");
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
