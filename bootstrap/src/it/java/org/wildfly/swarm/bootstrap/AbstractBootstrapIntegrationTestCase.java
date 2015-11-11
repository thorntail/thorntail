package org.wildfly.swarm.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractBootstrapIntegrationTestCase {

    protected ClassLoader createClassLoader(JavaArchive archive) throws IOException {
        File tmpFile = export(archive);
        return new URLClassLoader(new URL[]{tmpFile.toURI().toURL()}, null);
    }

    protected File export(JavaArchive archive) throws IOException {
        File tmpFile = File.createTempFile("boostrap-archive", ".jar");
        tmpFile.deleteOnExit();
        tmpFile.delete();
        archive.as(ZipExporter.class).exportTo(tmpFile);
        return tmpFile;
    }

    protected JavaArchive createBootstrapArchive() throws IOException {
        return createBootstrapArchive(null);
    }
    protected JavaArchive createBootstrapArchive(String mainClassName) throws IOException {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.as(ZipImporter.class).importFrom(new JarFile(findBootstrapJar()));
        archive.addAsManifestResource(EmptyAsset.INSTANCE, "wildfly-swarm.properties");

        if (mainClassName != null  ) {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(new Attributes.Name("Wildfly-Swarm-Main-Class"), mainClassName );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            manifest.write(out);
            out.close();
            archive.addAsManifestResource(new ByteArrayAsset(out.toByteArray()), "MANIFEST.MF");
        }
        return archive;
    }

    protected File findBootstrapJar() {
        Path targetDir = Paths.get("target");

        File[] children = targetDir.toFile().listFiles();
        for (File child : children) {
            if (child.getName().startsWith("wildfly-swarm-bootstrap") && child.getName().endsWith(".jar") && !child.getName().endsWith("-sources.jar")) {
                return child;
            }
        }

        return null;
    }
}
