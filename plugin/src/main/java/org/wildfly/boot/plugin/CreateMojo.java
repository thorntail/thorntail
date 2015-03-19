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

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}")
    private List<ArtifactRepository> remoteRepositories;

    @Inject
    private ArtifactResolver resolver;

    @Parameter(defaultValue = "${plugin.artifacts}")
    private List<Artifact> pluginArtifacts;


    private List<Artifact> featurePacks = new ArrayList<>();
    private Set<String> modules = new HashSet<>();

    private Set<ArtifactSpec> gavs = new HashSet<>();
    private List<String> fractionModules = new ArrayList();

    private File dir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setupFeaturePacks();
        setupDirectory();
        addJBossModules();
        addBootstrap();

        addFractions();
        addMavenRepository();

        addProjectArtifact();
        createJar();
    }

    private void setupFeaturePacks() {
        for (Artifact each : this.pluginArtifacts) {
            if (each.getArtifactId().contains("feature-pack") && each.getType().equals("zip")) {
                this.featurePacks.add(each);
            }
        }
    }

    private void addMavenRepository() throws MojoFailureException {
        File modulesDir = new File(this.dir, "modules");

        analyzeModuleXmls(modulesDir);
        collectArtifacts();
    }

    private void collectArtifacts() throws MojoFailureException {
        for (ArtifactSpec each : this.gavs) {
            if (!collectArtifact(each)) {
                System.err.println("unable to locate artifact: " + each);
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
        File m2repo = new File(this.dir, "m2repo");
        File dest = new File(m2repo, ArtifactUtils.toPath(artifact));

        dest.getParentFile().mkdirs();

        try {
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
        } catch (IOException e) {
            throw new MojoFailureException("unable to add artifact: " + dest, e);
        }
    }

    private Artifact locateArtifact(ArtifactSpec spec) {
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

    private void analyzeModuleXmls(File path) throws MojoFailureException {
        if (path.isDirectory()) {
            File[] children = path.listFiles();
            for (int i = 0; i < children.length; ++i) {
                analyzeModuleXmls(children[i]);
            }
        } else if (path.getName().equals("module.xml")) {
            analyzeModuleXml(path);
        }
    }

    private void analyzeModuleXml(File path) throws MojoFailureException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            try {
                String line = null;

                while ((line = in.readLine()) != null) {
                    int start = line.indexOf("${");
                    if (start >= 0) {
                        int end = line.indexOf("}");
                        if (end > 0) {
                            String gav = line.substring(start + 2, end);
                            this.gavs.add(new ArtifactSpec(gav));
                        }
                    }
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new MojoFailureException("Unable to analyze: " + path, e);
        }
    }

    private void addFractions() throws MojoFailureException {
        Set<Artifact> artifacts = this.project.getArtifacts();

        List<org.eclipse.aether.artifact.Artifact> fractions = new ArrayList<>();

        for (Artifact each : artifacts) {
            ArtifactRequest request = new ArtifactRequest();
            org.eclipse.aether.artifact.DefaultArtifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(each.getGroupId(), each.getArtifactId(), "fraction", "zip", each.getVersion());
            request.setArtifact(artifact);
            request.setRepositories(remoteRepositories());
            try {
                ArtifactResult result = resolver.resolveArtifact(this.session, request);
                fractions.add(result.getArtifact());
            } catch (ArtifactResolutionException e) {
                // skip
            }
        }

        for (org.eclipse.aether.artifact.Artifact each : fractions) {
            try {
                expandFraction(each);
            } catch (IOException e) {
                throw new MojoFailureException("error expanding feature-pack", e);
            }
        }
    }

    private static final String MODULE_PREFIX = "modules/system/layers/base/";
    private static final String MODULE_SUFFIX = "/main/module.xml";

    private void expandFraction(org.eclipse.aether.artifact.Artifact artifact) throws IOException {

        ZipFile zipFile = new ZipFile(artifact.getFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry each = entries.nextElement();

            File fsEach = new File(this.dir, each.getName());

            if (each.isDirectory()) {
                fsEach.mkdirs();
                continue;
            }

            if (each.getName().startsWith(MODULE_PREFIX) && each.getName().endsWith(MODULE_SUFFIX)) {
                String moduleName = each.getName().substring(MODULE_PREFIX.length(), each.getName().length() - MODULE_SUFFIX.length());

                moduleName = moduleName.replaceAll("/", ".") + ":main";
                this.fractionModules.add( moduleName );
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

            if (fsEach.getName().equals("module.xml")) {
                addTransitiveModules(fsEach);
            }
        }
    }

    private final String TARGET_NAME_PREFIX = "target-name=\"";

    private void addTransitiveModules(File moduleXml) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(moduleXml)));
            try {

                String line = null;

                while ((line = in.readLine()) != null) {
                    line = line.trim();

                    if ( line.startsWith( "<module-alias" ) ) {
                        int start = line.indexOf( TARGET_NAME_PREFIX );
                        if ( start > 0 ) {
                            int end = line.indexOf( "\"", start + TARGET_NAME_PREFIX.length() );
                            if ( end >= 0 ) {
                                String moduleName = line.substring( start + TARGET_NAME_PREFIX.length(), end );
                                addTransitiveModule(moduleName);
                                break;
                            }
                        }
                    }

                    if (line.startsWith("<module name=")) {

                        int start = line.indexOf("\"");
                        if (start > 0) {
                            int end = line.indexOf("\"", start + 1);
                            if (end > 0) {
                                String moduleName = line.substring(start + 1, end);
                                if ( ! line.contains( "optional=\"true\"" ) ) {
                                    addTransitiveModule(moduleName);
                                }
                            }
                        }
                    }
                }

            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTransitiveModule(String moduleName) {
        if (this.modules.contains(moduleName)) {
            return;
        }

        String search = "modules/system/layers/base/" + moduleName.replaceAll("\\.", "/") + "/main/module.xml";

        for (Artifact pack : this.featurePacks) {
            try {
                ZipFile zip = new ZipFile(pack.getFile());
                Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry each = entries.nextElement();

                    if (each.getName().equals(search)) {
                        File outFile = new File(this.dir, search);
                        outFile.getParentFile().mkdirs();
                        InputStream in = zip.getInputStream(each);
                        try {
                            FileOutputStream out = new FileOutputStream(outFile);
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
                        this.modules.add(moduleName);
                        addTransitiveModules(outFile);
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    private void addJBossModules() throws MojoFailureException {
        Artifact artifact = findArtifact("org.jboss.modules", "jboss-modules", "jar");
        try {
            expandArtifact(artifact);
        } catch (IOException e) {
            throw new MojoFailureException("Unable to add jboss-modules");
        }
    }


    private void expandArtifact(Artifact artifact) throws IOException {

        File destination = this.dir;

        File artifactFile = artifact.getFile();

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

    private Artifact findArtifact(String groupId, String artifactId, String type) {
        Set<Artifact> artifacts = this.project.getArtifacts();

        for (Artifact each : artifacts) {
            if (each.getGroupId().equals(groupId) && each.getArtifactId().equals(artifactId) && each.getType().equals(type)) {
                return each;
            }
        }

        return null;
    }

    private void addBootstrap() throws MojoFailureException {
        Artifact artifact = findArtifact("org.wildfly.boot", "wildfly-boot-bootstrap", "jar");
        try {
            expandArtifact(artifact);
        } catch (IOException e) {
            throw new MojoFailureException("Unable to add bootstrap");
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


    private Manifest createManifest() throws MojoFailureException {
        Manifest manifest = new Manifest();

        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, "org.wildfly.boot.bootstrap.Main");
        attrs.putValue("Application-Artifact", this.project.getArtifact().getFile().getName());

        StringBuilder modules = new StringBuilder();
        boolean first = true;

        for ( String each : this.fractionModules ) {
            if ( ! first ) {
                modules.append( "," );
            }

            modules.append( each );
            first = false;
        }
        attrs.putValue("Feature-Pack-Modules", modules.toString() );

        return manifest;
    }


    private List<RemoteRepository> remoteRepositories() {
        List<RemoteRepository> repos = new ArrayList<>();

        for (ArtifactRepository each : this.remoteRepositories) {
            RemoteRepository.Builder builder = new RemoteRepository.Builder(each.getId(), "default", each.getUrl());
            repos.add(builder.build());
        }

        return repos;
    }

    /*
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



    private void recordFeaturePackModuleNam(ZipFile zipFile, ZipEntry entry) throws IOException {
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




    private String createPath(Artifact artifact) {

        String path = artifact.getGroupId().replaceAll("\\.", File.separator);

        path = path + File.separator + artifact.getArtifactId();
        path = path + File.separator + artifact.getVersion();
        path = path + File.separator + artifact.getFile().getName();

        return path;
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
    */
}
