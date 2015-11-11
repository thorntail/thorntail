package org.wildfly.swarm.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.util.Layout;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class LayoutIT {

    @Test
    public void testIsUberJar() throws Exception {
        JavaArchive archive = createBootstrapArchive();

        archive.addAsManifestResource(EmptyAsset.INSTANCE, "wildfly-swarm.properties");

        ClassLoader cl = createClassLoader(archive);
        Class<?> layoutClass = cl.loadClass(Layout.class.getName());

        Method getInstance = layoutClass.getMethod("getInstance");
        Object layout = getInstance.invoke(layoutClass);

        Method isUberJar = layoutClass.getMethod("isUberJar");
        Object result = isUberJar.invoke(layout);

        assertThat(result).isEqualTo(true);
    }

    @Test
    public void testGetManifest() throws Exception {
        JavaArchive archive = createBootstrapArchive();

        archive.addAsManifestResource(EmptyAsset.INSTANCE, "wildfly-swarm.properties");

        Manifest manifest = new Manifest();

        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(new Attributes.Name("Wildfly-Swarm-Main-Class"), "MyMainClass");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        manifest.write(out);
        out.close();
        archive.addAsManifestResource(new ByteArrayAsset(out.toByteArray()), "MANIFEST.MF");

        ClassLoader cl = createClassLoader(archive);
        Class<?> layoutClass = cl.loadClass(Layout.class.getName());

        Method getInstance = layoutClass.getMethod("getInstance");
        Object layout = getInstance.invoke(layoutClass);

        Method isUberJar = layoutClass.getMethod("isUberJar");
        Object result = isUberJar.invoke(layout);

        assertThat(result).isEqualTo(true);

        Method getManifest = layoutClass.getMethod("getManifest");
        Manifest fetchedManifest = (Manifest) getManifest.invoke(layout);

        assertThat(fetchedManifest).isNotNull();
        assertThat(fetchedManifest.getMainAttributes().get(new Attributes.Name("Wildfly-Swarm-Main-Class"))).isEqualTo("MyMainClass");
    }

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
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.as(ZipImporter.class).importFrom(new JarFile(findBootstrapJar()));
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
