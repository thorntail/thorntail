package org.wildfly.swarm;

import org.jboss.modules.MavenArtifactUtil;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
        List<JavaArchive> archives = new ArrayList<>();

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
                        archives.add(archive);
                    }
                }
            }
        } else {
            String classpath = System.getProperty("java.class.path");
            String javaHome = System.getProperty("java.home");
            Path pwd = Paths.get(System.getProperty("user.dir"));
            if (classpath != null) {
                String[] elements = classpath.split(File.pathSeparator);

                for (int i = 0; i < elements.length; ++i) {
                    if (!elements[i].startsWith(javaHome)) {
                        File artifact = new File(elements[i]);
                        if (artifact.isFile()) {
                            JavaArchive archive = ShrinkWrap.create(JavaArchive.class, artifact.getName());
                            new ZipImporterImpl(archive).importFrom(artifact);
                            archives.add(archive);
                        } else {
                            if (artifact.toPath().startsWith(pwd)) {
                                continue;
                            }

                            JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
                            Path basePath = artifact.toPath();
                            Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    //System.err.println(  basePath.relativize(file).toString() );
                                    archive.add(new FileAsset(file.toFile()), basePath.relativize(file).toString());
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                            archives.add(archive);
                        }
                    }
                }
            }

        }

        return archives;
    }

    private static File findFile(String gav) throws IOException, ModuleLoadException {

        // groupId:artifactId
        // groupId:artifactId:version
        // groupId:artifactId:packaging:version
        // groupId:artifactId:packaging:version:classifier

        String[] parts = gav.split(":");

        if (parts.length < 2) {
            throw new RuntimeException("GAV must includes at least 2 segments");
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String packaging = "jar";
        String version = null;
        String classifier = "";

        if (parts.length == 3) {
            version = parts[2];
        }

        if (parts.length == 4) {
            packaging = parts[2];
            version = parts[3];
        }

        if (parts.length == 5) {
            packaging = parts[2];
            version = parts[3];
            classifier = parts[4];
        }

        if (version.isEmpty() || version.equals("*")) {
            version = null;
        }

        if (version == null) {
            version = determineVersionViaDependenciesTxt(groupId, artifactId, packaging, classifier);
        }

        if (version == null) {
            version = determineVersionViaClasspath(groupId, artifactId, packaging, classifier);
        }

        if (version == null) {
            throw new RuntimeException("Unable to determine version number from 2-part GAV.  Try three!");
        }

        System.err.println( "found version: " + version );

        return MavenArtifactUtil.resolveArtifact(groupId + ":" + artifactId + ":" + version + (classifier == null ? "" : ":" + classifier), packaging);
    }

    private static String determineVersionViaDependenciesTxt(String groupId, String artifactId, String packaging, String classifier) throws IOException {

        if (packaging.equals("jar")) {
            InputStream depsTxt = ClassLoader.getSystemClassLoader().getResourceAsStream("META-INF/wildfly-swarm-dependencies.txt");

            if (depsTxt != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(depsTxt))) {

                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            String[] parts = line.split(":");
                            if (parts.length >= 3) {
                                if (parts[0].equals(groupId) && parts[1].equals(artifactId)) {
                                    return parts[2];
                                }
                            }
                        }
                    }
                }
            }
        }

        InputStream depsTxt = ClassLoader.getSystemClassLoader().getResourceAsStream("META-INF/wildfly-swarm-extra-dependencies.txt");

        if (depsTxt != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(depsTxt))) {

                String line = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        String[] parts = line.split(":");
                        if (parts.length == 4) {
                            if (parts[0].equals(groupId) && parts[1].equals(artifactId) && parts[2].equals(packaging)) {
                                return parts[3];
                            }
                        }
                        if (parts.length == 5) {
                            if (parts[0].equals(groupId) && parts[1].equals(artifactId) && parts[2].equals(packaging) && parts[4].equals(classifier)) {
                                return parts[3];
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static String determineVersionViaClasspath(String groupId, String artifactId, String packaging, String classifier) {

        String regexp = ".*" + artifactId + "-(.+)" + (classifier.length() == 0 ? "" : "-" + classifier) + "." + packaging;
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
