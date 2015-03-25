package org.wildfly.boot.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.aether.impl.ArtifactResolver;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(
        name = "analyze",
        requiresDependencyCollection = ResolutionScope.COMPILE,
        requiresDependencyResolution = ResolutionScope.COMPILE
)
public class AnalyzeMojo extends AbstractSwarmMojo {

    @Inject
    private ArtifactResolver resolver;

    private ModuleNode root = new ModuleNode("");
    private Map<String, ModuleNode> modules = new HashMap<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        setupFeaturePacks(this.resolver);
        processFractions(this.resolver, new FractionAnalyzer());
        display();
    }

    private void display() {
        this.root.dump();
    }

    private void addTransitiveModules(ModuleNode parent, ZipFile zipFile, ZipEntry moduleXml) {
        String currentName = moduleXml.getName().substring(MODULE_PREFIX.length(), moduleXml.getName().length() - MODULE_SUFFIX.length());
        currentName = currentName.replaceAll("/", ".");

        if (this.modules.containsKey(currentName)) {
            parent.addChild(this.modules.get(currentName));
            return;
        }

        ModuleNode current = new ModuleNode(currentName);
        this.modules.put(currentName, current);
        parent.addChild(current);

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(zipFile.getInputStream(moduleXml)));
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
        String search = "modules/system/layers/base/" + moduleName.replaceAll("\\.", "/") + "/main/module.xml";

        for (Artifact pack : this.featurePacks) {
            try {
                ZipFile zip = new ZipFile(pack.getFile());
                Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry each = entries.nextElement();

                    if (each.getName().equals(search)) {
                        addTransitiveModules(parent, zip, each);
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    final class FractionAnalyzer implements ExceptionConsumer<org.eclipse.aether.artifact.Artifact> {
        @Override
        public void accept(org.eclipse.aether.artifact.Artifact artifact) throws Exception {
            ZipFile zipFile = new ZipFile(artifact.getFile());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry each = entries.nextElement();

                if (each.getName().startsWith(MODULE_PREFIX) && each.getName().endsWith(MODULE_SUFFIX)) {
                    System.err.println("Analyze: " + each);
                    addTransitiveModules(root, zipFile, each);
                }
            }
        }
    }
}
