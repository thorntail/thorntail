package org.wildfly.swarm.bootstrap.util;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @author Bob McWhirter
 */
public class Layout {

    private static ClassLoader BOOTSTRAP_CLASSLOADER = null;

    public static boolean isFatJar() throws IOException {
        Path root = getRoot();

        if ( Files.isRegularFile( root ) ) {
            try ( JarFile jar = new JarFile( root.toFile() ) ) {
                ZipEntry props = jar.getEntry("META-INF/wildfly-swarm.properties");
                if ( props != null ) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Path getRoot() throws IOException {
        URL location = Layout.class.getProtectionDomain().getCodeSource().getLocation();
        if ( location.getProtocol().equals( "file" ) ) {
            Path path = Paths.get(location.getPath());
            return path;
        }

        throw new IOException("Unable to determine root" );
    }

    public static Manifest getManifest() throws IOException {
        Path root = getRoot();
        if ( isFatJar() ) {
            try ( JarFile jar = new JarFile( root.toFile()) ) {
                ZipEntry entry = jar.getEntry("META-INF/MANIFEST.MF");
                if ( entry != null ) {
                    InputStream in = jar.getInputStream(entry);
                    return new Manifest(in);
                }
            }
        }

        return null;
    }

    public synchronized static ClassLoader getBootstrapClassLoader() throws ModuleLoadException {
        System.err.println( Thread.currentThread() + " get BOOTSTRAP " + System.identityHashCode( Layout.class.getClassLoader() ) );
        if ( BOOTSTRAP_CLASSLOADER == null ) {
            System.err.println( "SOLVE BOOTSTRAP" );
            try {
                System.err.println( "PRE GET SOLVE BOOTSTRAP" );
                BOOTSTRAP_CLASSLOADER = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.bootstrap")).getClassLoader();
                System.err.println( "POST GET SOLVE BOOTSTRAP" );
            } catch (ModuleLoadException e) {
                System.err.println( "FALLBACK" );
                System.err.println( "FALLBACK" );
                System.err.println( "FALLBACK" );
                System.err.println( "FALLBACK" );
                System.err.println( "FALLBACK" );
                System.err.println( "FALLBACK" );
                BOOTSTRAP_CLASSLOADER = Layout.class.getClassLoader();
            } catch (Throwable t) {
                System.err.println( "SHIT" );
                t.printStackTrace();
            }
        }
        System.err.println( "BOOTSTRAP IS " + BOOTSTRAP_CLASSLOADER );

        return BOOTSTRAP_CLASSLOADER;
    }
}
