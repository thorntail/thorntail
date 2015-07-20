package org.wildfly.swarm;

import org.jboss.modules.MavenArtifactUtil;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bob McWhirter
 */
public class ArtifactManager {

    public static JavaArchive artifact(String gav) throws IOException, ModuleLoadException {
        File file = findFile(gav);
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, file.getName());
        new ZipImporterImpl(archive).importFrom(file);
        return archive;
    }

    public static JavaArchive artifact(String gav, String asName) throws IOException, ModuleLoadException {
        File file = findFile(gav);
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, asName);
        new ZipImporterImpl(archive).importFrom(file);
        return archive;
    }

    public static List<JavaArchive> allArtifacts() throws IOException {
        List<JavaArchive> archives  = new ArrayList<>();

        InputStream depsTxt = ClassLoader.getSystemClassLoader().getResourceAsStream("META-INF/wildfly-swarm-dependencies.txt");

        if (depsTxt != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(depsTxt))) {

                String line = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        File artifact = MavenArtifactUtil.resolveJarArtifact(line);

                        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, artifact.getName());
                        new ZipImporterImpl(archive).importFrom(artifact);
                        archives.add( archive );
                    }
                }
            }
        } else {
            String classpath = System.getProperty("java.class.path");
            String javaHome = System.getProperty("java.home");
            if (classpath != null) {
                String[] elements = classpath.split(File.pathSeparator);

                for (int i = 0; i < elements.length; ++i) {
                    if (!elements[i].startsWith(javaHome)) {
                        File artifact = new File(elements[i]);
                        if (artifact.isFile()) {
                            JavaArchive archive = ShrinkWrap.create(JavaArchive.class, artifact.getName());
                            new ZipImporterImpl(archive).importFrom(artifact);
                            archives.add( archive );
                        }
                    }
                }
            }

        }

        return archives;
    }

    private static File findFile(String gav) throws IOException, ModuleLoadException {
        String[] parts = gav.split(":");

        if (parts.length < 2) {
            throw new RuntimeException("GAV must includes at least 2 segments");
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = null;
        String classifier = "";

        if (parts.length > 3) {
            classifier = parts[3];
        }

        if (parts.length > 2) {
            version = parts[2];
        } else {
            version = determineVersionViaModule(groupId, artifactId, classifier);
        }

        if (version == null) {
            version = determineVersionViaClasspath(groupId, artifactId, classifier);
        }

        if (version == null) {
            throw new RuntimeException("Unable to determine version number from 2-part GAV.  Try three!");
        }

        return MavenArtifactUtil.resolveJarArtifact(groupId + ":" + artifactId + ":" + version + (classifier == null ? "" : ":" + classifier));
    }

    private static String determineVersionViaModule(String groupId, String artifactId, String classifier) throws ModuleLoadException {
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("APP", "dependencies"));
        return module.getProperty("version." + groupId + ":" + artifactId + "::" + classifier);
    }

    private static String determineVersionViaClasspath(String groupId, String artifactId, String classifier) {

        String regexp = ".*" + artifactId + "-(.+)" + (classifier.length() == 0 ? "" : "-" + classifier) + ".jar";
        Pattern pattern = Pattern.compile(regexp);

        String classpath = System.getProperty("java.class.path");
        String[] elements = classpath.split(File.pathSeparator);

        for (int i = 0; i < elements.length; ++i) {
            Matcher matcher = pattern.matcher(elements[i]);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        return null;
    }

}
