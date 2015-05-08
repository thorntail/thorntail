package org.jboss.modules;

import org.wildfly.swarm.bootstrap.m2.JarRepositoryResolver;
import org.wildfly.swarm.bootstrap.m2.LocalRepositoryResolver;
import org.wildfly.swarm.bootstrap.m2.RepositoryResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * @author Bob McWhirter
 */
public class ArtifactLoaderFactory {

    public static final ArtifactLoaderFactory INSTANCE = new ArtifactLoaderFactory();

    private Map<String, ResourceLoader> loaders = new HashMap<>();

    private RepositoryResolver[] resolvers = new RepositoryResolver[]{
            new JarRepositoryResolver(),
            new LocalRepositoryResolver(),
    };

    public ArtifactLoaderFactory() {
    }

    public synchronized ResourceLoader getLoader(String gav) throws IOException {
        ResourceLoader loader = this.loaders.get(gav);
        if (loader != null) {
            return loader;
        }

        File jarFile = getFile(gav);
        if (jarFile == null) {
            return null;
        }
        loader = ResourceLoaders.createJarResourceLoader(gav, new JarFile(jarFile));
        this.loaders.put(gav, loader);
        return loader;
    }

    public File getFile(String gav) throws IOException {
        File file = null;
        for ( int i = 0 ; i < this.resolvers.length ; ++i ) {
            file = this.resolvers[i].resolve(gav);
            if ( file != null ) {
                return file;
            }
        }

        return null;
    }

}
