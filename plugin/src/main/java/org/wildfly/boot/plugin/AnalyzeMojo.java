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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        name = "analyze",
        requiresDependencyCollection = ResolutionScope.COMPILE,
        requiresDependencyResolution = ResolutionScope.COMPILE
)
public class AnalyzeMojo extends AbstractMojo {

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

    private Set<ArtifactSpec> gavs = new HashSet<>();

    private ModuleNode root = new ModuleNode( "" );
    private Map<String,ModuleNode> modules = new HashMap<>();

    private File dir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setupFeaturePacks();
        analyzeFractions();
        display();
    }

    private void display() {
        this.root.dump();
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

    private void analyzeFractions() throws MojoFailureException {
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
                analyzeFraction(each);
            } catch (IOException e) {
                throw new MojoFailureException("error expanding feature-pack", e);
            }
        }
    }

    private static final String MODULE_PREFIX = "modules/system/layers/base/";
    private static final String MODULE_SUFFIX = "/main/module.xml";

    private void analyzeFraction(org.eclipse.aether.artifact.Artifact artifact) throws IOException {

        ZipFile zipFile = new ZipFile(artifact.getFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry each = entries.nextElement();

            if (each.getName().startsWith(MODULE_PREFIX) && each.getName().endsWith(MODULE_SUFFIX)) {
                System.err.println( "Analyze: " + each );
                addTransitiveModules(this.root, zipFile, each);
            }

        }
    }

    private final String TARGET_NAME_PREFIX = "target-name=\"";

    private void addTransitiveModules(ModuleNode parent, ZipFile zipFile, ZipEntry moduleXml) {
        String currentName = moduleXml.getName().substring(MODULE_PREFIX.length(), moduleXml.getName().length() - MODULE_SUFFIX.length());
        currentName = currentName.replaceAll("/", ".");

        if ( this.modules.containsKey( currentName ) ) {
            parent.addChild( this.modules.get( currentName ) );
            return;
        }

        ModuleNode current = new ModuleNode( currentName );
        this.modules.put( currentName, current );
        parent.addChild( current );

        try {
            BufferedReader in = new BufferedReader( new InputStreamReader( zipFile.getInputStream(moduleXml) ) );
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
                                addTransitiveModule(current, moduleName);
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
                                    addTransitiveModule(current, moduleName);
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

    private void addTransitiveModule(ModuleNode parent, String moduleName) {
        //if (this.modules.contains(moduleName)) {
            //return;
        //}

        //this.modules.add( moduleName );

        String search = "modules/system/layers/base/" + moduleName.replaceAll("\\.", "/") + "/main/module.xml";

        for (Artifact pack : this.featurePacks) {
            try {
                ZipFile zip = new ZipFile(pack.getFile());
                Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry each = entries.nextElement();

                    if (each.getName().equals(search)) {
                        addTransitiveModules(  parent, zip, each );
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private List<RemoteRepository> remoteRepositories() {
        List<RemoteRepository> repos = new ArrayList<>();

        for (ArtifactRepository each : this.remoteRepositories) {
            RemoteRepository.Builder builder = new RemoteRepository.Builder(each.getId(), "default", each.getUrl());
            repos.add(builder.build());
        }

        return repos;
    }

}
