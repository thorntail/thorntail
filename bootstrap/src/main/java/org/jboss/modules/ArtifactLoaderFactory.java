package org.jboss.modules;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Bob McWhirter
 */
public class ArtifactLoaderFactory {

    public static final ArtifactLoaderFactory INSTANCE = new ArtifactLoaderFactory();

    private Map<String, ResourceLoader> loaders = new HashMap<>();

    public ArtifactLoaderFactory() {
    }

    public synchronized ResourceLoader getLoader(String gav) throws IOException {
        ResourceLoader loader = this.loaders.get(gav);
        if ( loader != null ) {
            return loader;
        }

        File jarFile = getFile( gav );
        if ( jarFile == null ) {
            return null;
        }
        loader = ResourceLoaders.createJarResourceLoader( gav, new JarFile(jarFile));
        this.loaders.put( gav, loader );
        return loader;
    }

    private File getFile(String gav) throws IOException {
//        System.err.println( "gavToPath: " + gavToPath(gav));
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(gavToPath(gav));
        if ( in == null ) {
            return null;
        }

        try {
            File tmp = File.createTempFile(gav, ".jar");

            FileOutputStream out = new FileOutputStream(tmp);

            try {
                byte[] buf = new byte[1024];
                int len = -1;

                while ((len = in.read(buf)) >= 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
            return tmp;
        } finally {
            in.close();
        }
    }

    private String gavToPath(String gav) {
        try {
            String[] parts = gav.split(":");
            return "m2repo/" + parts[0].replaceAll("\\.", "/") + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] + ".jar";
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println( "-----------------" );
            System.err.println( "-----------------" );
            System.err.println( gav );
            System.err.println( "-----------------" );
            System.err.println( "-----------------" );
            throw e;
        }
    }

}
