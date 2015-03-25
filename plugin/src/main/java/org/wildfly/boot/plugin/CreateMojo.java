package org.wildfly.boot.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.ProjectBuildingResult;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.ArtifactResolver;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(
        name = "create",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE,
        requiresDependencyResolution = ResolutionScope.COMPILE
)
public class CreateMojo extends AbstractSwarmMojo {

    @Inject
    private ArtifactResolver resolver;

    private Set<String> modules = new HashSet<>();

    private List<String> fractionModules = new ArrayList();

    private File dir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setupFeaturePacks();
        setupFeaturePackArtifacts();
        setupDirectory();
        addJBossModules();
        addBootstrap();

        processFractions(this.resolver, new FractionExpander());
        addMavenRepository();

        addProjectArtifact();
        createJar();
    }

    private void setupFeaturePackArtifacts() throws MojoFailureException {
        for (Artifact pack : this.featurePacks) {
            Artifact packPom = new DefaultArtifact(pack.getGroupId(),
                    pack.getArtifactId(),
                    pack.getVersion(),
                    "",
                    "pom",
                    pack.getClassifier(),
                    new DefaultArtifactHandler("pom"));

            try {
                ProjectBuildingResult buildingResult = buildProject(packPom);
                DependencyResolutionResult resolutionResult = resolveProjectDependencies(buildingResult.getProject(), null);

                if (resolutionResult.getDependencies() != null && resolutionResult.getDependencies().size() > 0) {
                    for (Dependency dep : resolutionResult.getDependencies()) {
                        this.featurePackArtifacts.add(convertAetherToMavenArtifact(dep.getArtifact(), "compile", "jar"));
                    }
                }
            } catch (Exception e) {
                // skip
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
                    copyContent(in, out);
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

    private void addTransitiveModules(File moduleXml) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(moduleXml)));
            try {

                String line = null;

                while ((line = in.readLine()) != null) {
                    line = line.trim();

                    if (line.startsWith("<module-alias")) {
                        int start = line.indexOf(TARGET_NAME_PREFIX);
                        if (start > 0) {
                            int end = line.indexOf("\"", start + TARGET_NAME_PREFIX.length());
                            if (end >= 0) {
                                String moduleName = line.substring(start + TARGET_NAME_PREFIX.length(), end);
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
                                if (!line.contains("optional=\"true\"")) {
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
                        copyFileFromZip(zip, each, outFile);
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
                    copyFileFromZip(jarFile, each, fsEach);
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
                    copyContent(in, out);
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
                copyContent(in, out);
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

        for (String each : this.fractionModules) {
            if (!first) {
                modules.append(",");
            }

            modules.append(each);
            first = false;
        }
        attrs.putValue("Feature-Pack-Modules", modules.toString());

        return manifest;
    }

    final class FractionExpander implements ExceptionConsumer<org.eclipse.aether.artifact.Artifact> {
        @Override
        public void accept(org.eclipse.aether.artifact.Artifact artifact) throws Exception {
            ZipFile zipFile = new ZipFile(artifact.getFile());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry each = entries.nextElement();

                File fsEach = new File(dir, each.getName());

                if (each.isDirectory()) {
                    fsEach.mkdirs();
                    continue;
                }

                if (each.getName().startsWith(MODULE_PREFIX) && each.getName().endsWith(MODULE_SUFFIX)) {
                    String moduleName = each.getName().substring(MODULE_PREFIX.length(), each.getName().length() - MODULE_SUFFIX.length());

                    moduleName = moduleName.replaceAll("/", ".") + ":main";
                    fractionModules.add(moduleName);
                }

                copyFileFromZip(zipFile, each, fsEach);

                if (fsEach.getName().equals("module.xml")) {
                    addTransitiveModules(fsEach);
                }
            }
        }
    }

}
