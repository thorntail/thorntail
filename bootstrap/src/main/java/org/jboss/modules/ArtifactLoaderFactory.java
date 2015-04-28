package org.jboss.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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

    public File getFile(String gav) throws IOException {
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

    private static final String JANDEX_SUFFIX = "?jandex";

    public String gavToPath(String gav) {
        try {
            String[] parts = gav.split(":");
            String group = parts[0];
            String artifact = parts[1];
            String version = parts[2];
            String classifier = null;
            if ( parts.length >= 4 ) {
                classifier = parts[3];
            }

            if ( artifact.endsWith( JANDEX_SUFFIX ) ) {
                artifact = artifact.substring( 0, artifact.length() - JANDEX_SUFFIX.length() );
            }

            String path = "m2repo/" + group.replaceAll("\\.", "/") + "/" + artifact + "/" + version + "/" + artifact + "-" + version + ( classifier == null || classifier.equals( "" ) ? "" : "-" + classifier) + ".jar";
            return path;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println( "-----------------" );
            System.err.println( gav );
            System.err.println( "-----------------" );
            throw e;
        }
    }

}
