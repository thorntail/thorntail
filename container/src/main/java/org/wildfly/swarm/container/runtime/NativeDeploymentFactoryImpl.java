package org.wildfly.swarm.container.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.enterprise.inject.Vetoed;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.bootstrap.env.NativeDeploymentFactory;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class NativeDeploymentFactoryImpl implements NativeDeploymentFactory {

    public static String APP_NAME = "swarm.app.name";

    public static String APP_PATH = "swarm.app.path";

    public static String APP_ARTIFACT = "swarm.app.artifact";

    private String name;

    public NativeDeploymentFactoryImpl() {
    }

    public Archive nativeDeployment() throws IOException {
        final String appPath = System.getProperty(APP_PATH);
        if (appPath != null) {
            return setupUsingAppPath(appPath);
        }

        final String appArtifact = System.getProperty(APP_ARTIFACT);
        if (appArtifact != null) {
            return setupUsingAppArtifact(appArtifact);
        }

        return null;
    }

    public Archive createEmptyArchive(Class<? extends Archive> type, String suffix) {
        return ShrinkWrap.create(type, determineName(suffix)).as(type);
    }

    protected Archive setupUsingAppPath(String appPath) throws IOException {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, determineName(null));
        final Path path = Paths.get(appPath);
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path simple = path.relativize(file);
                    archive.add(new FileAsset(file.toFile()), convertSeparators(simple));
                    return super.visitFile(file, attrs);
                }
            });
        } else {
            archive.as(ZipImporter.class)
                    .importFrom(path.toFile());
        }
        return archive;
    }

    protected Archive setupUsingAppArtifact(String appArtifact) throws IOException {
        try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("_bootstrap/" + appArtifact)) {
            if (in != null) {
                JavaArchive archive = ShrinkWrap.create(JavaArchive.class, determineName(null));
                archive.as(ZipImporter.class)
                        .importFrom(in);
                return archive;
            }
        }
        return null;
    }

    protected String convertSeparators(Path path) {
        String convertedPath = path.toString();

        if (convertedPath.contains(File.separator)) {
            convertedPath = convertedPath.replace(File.separator, "/");
        }

        return convertedPath;
    }

    protected static String swapSuffix(String name, String suffix) {
        int lastDotLoc = name.lastIndexOf('.');
        if (lastDotLoc < 0) {
            return name + suffix;
        }

        return name.substring(0, lastDotLoc) + suffix;
    }

    protected synchronized String determineName(final String suffix) {
        if (this.name != null) {
            return this.name;
        }

        String appName = System.getProperty(APP_NAME);
        String appPath = System.getProperty(APP_PATH);
        String appArtifact = System.getProperty(APP_ARTIFACT);
        String name = null;

        if (appName != null) {
            name = appName;
        } else if (appPath != null) {
            final File file = new File(appPath);
            name = file.getName();
        } else if (appArtifact != null) {
            name = appArtifact;
        }

        if (suffix != null) {
            if (!name.endsWith(suffix)) {
                name = swapSuffix(name, suffix);
            }
        }

        this.name = name;
        System.setProperty(APP_ARTIFACT, name);
        return name;
    }
}
