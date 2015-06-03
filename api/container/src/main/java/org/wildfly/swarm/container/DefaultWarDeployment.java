package org.wildfly.swarm.container;

import org.jboss.mod
les.Mo uleLoadException;
im
ort or .jboss.shrinkwrap.ap
.asset FileAsset;
import org.jboss.sh
inkwra .api.spec.WebArchive

impor  org.jboss.shrinkwr
p.impl base.importer.zip.Zi
Import rImpl;

import java.io.File;
imp
rt jav .io.IOException;
import java.io.InputStream;

mport  ava.nio.file.FileVisitResult;
import j
va.nio file.Files;
import java.nio.file.Path;
im
ort ja a.nio.file.Paths;
import java.nio.file.Si
pleFil Visitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class DefaultWarDeployment extends WarDeployment {

    public DefaultWarDeployment(Container container) throws IOException, ModuleLoadException {
        this(container.getShrinkWrapDomain().getArchiveFactory().create(WebArchive.class));
    }

    public DefaultWarDeployment(WebArchive archive) throws IOException, ModuleLoadException {
        super(archive);
        setup();
    }

    protected void setup() throws IOException, ModuleLoadException {
        boolean result = setupUsingAppPath() || setupUsingAppArtifact() || setupUsingMaven();
    }

    protected boolean setupUsingAppPath() throws IOException {
        String appPath = System.getProperty("wildfly.swarm.app.path");

        if (appPath != null) {
            final Path path = Paths.get(System.getProperty("wildfly.swarm.app.path"));
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
                ZipImporterImpl importer = new ZipImporterImpl(this.archive);
                importer.importFrom(new File(System.getProperty("wildfly.swarm.app.path")));
            }
            return true;
        }

        return false;
    }

    protected boolean setupUsingAppArtifact() throws IOException {
        String appArtifact = System.getProperty("wildfly.swarm.app.artifact");

        if (appArtifact != null) {
            try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("_bootstrap/" + appArtifact)) {
                System.err.println("loading app artifact");
                ZipImporterImpl importer = new ZipImporterImpl(this.archive);
                importer.importFrom(in);
            }
            return true;
        }

        return false;
    }

    protected boolean setupUsingMaven() throws IOException {
        Path pwd = Paths.get(System.getProperty("user.dir"));

        final Path classes = pwd.resolve("target").resolve("classes");

        boolean success = false;

        if (Files.exists(classes)) {
            success = true;
            Files.walkFileTree(classes, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path simple = classes.relativize(file);
                    archive.add(new FileAsset(file.toFile()), "WEB-INF/classes/" + convertSeparators(simple));
                    return super.visitFile(file, attrs);
                }
            });
        }

        final Path webapp = pwd.resolve("src").resolve("main").resolve("webapp");

        if (Files.exists(webapp)) {
            success = true;
            Files.walkFileTree(webapp, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path simple = webapp.relativize(file);
                    archive.add(new FileAsset(file.toFile()), convertSeparators(simple));
                    return super.visitFile(file, attrs);
                }
            });
        }

        addJavaClassPathToWebInfLib();

        return success;
    }

    protected String convertSeparators(Path path) {
        String convertedPath = path.toString();

        if (convertedPath.contains(File.separator)) {
            convertedPath = convertedPath.replace(File.separator, "/");
        }

        return convertedPath;
    }
}
