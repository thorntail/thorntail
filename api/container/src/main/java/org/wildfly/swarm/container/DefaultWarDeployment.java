package org.wildfly.swarm.container;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public class DefaultWarDeployment implements Deployment {

    private final static String JBOSS_WEB_CONTENTS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<jboss-web>\n" +
                    "    <context-root>/</context-root>\n" +
                    "</jboss-web>";

    private final WebArchive archive;

    public DefaultWarDeployment(WebArchive archive) throws IOException, ModuleLoadException {
        this.archive = archive;
        setup();
    }

    protected void setup() throws IOException, ModuleLoadException {
        if (setupUsingAppPath() || setupUsingAppArtifact() || setupUsingMaven()) {
            ensureJBossWebXml();
        }
    }

    protected void ensureJBossWebXml() {
        if (this.archive.contains("WEB-INF/jboss-web.xml")) {
            return;
        }

        this.archive.add(new StringAsset(JBOSS_WEB_CONTENTS), "WEB-INF/jboss-web.xml");
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
                        archive.add(new FileAsset(file.toFile()), simple.toString());
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
                    archive.add(new FileAsset(file.toFile()), "WEB-INF/classes/" + simple.toString());
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
                    archive.add(new FileAsset(file.toFile()), simple.toString());
                    return super.visitFile(file, attrs);
                }
            });
        }

        String classpath = System.getProperty("java.class.path");
        String javaHome = System.getProperty("java.home");
        if (classpath != null) {
            String[] elements = classpath.split(File.pathSeparator);

            for (int i = 0; i < elements.length; ++i) {
                if (!elements[i].startsWith(javaHome) ) {
                    File file = new File(elements[i]);
                    if ( file.isFile() ) {
                        this.archive.add(new FileAsset(file), "WEB-INF/lib/" + file.getName());
                    }
                }
            }
        }

        return success;
    }

    public WebArchive getArchive() {
        return this.archive;
    }
}
