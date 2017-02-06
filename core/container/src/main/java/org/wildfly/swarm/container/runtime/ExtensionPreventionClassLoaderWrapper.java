package org.wildfly.swarm.container.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author Bob McWhirter
 */
public class ExtensionPreventionClassLoaderWrapper extends ClassLoader {

    private static final String EXTENSION = "META-INF/services/javax.enterprise.inject.spi.Extension";

    private final ClassLoader delegate;

    public ExtensionPreventionClassLoaderWrapper(ClassLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {

        if (name.equals(EXTENSION)) {
            return Collections.emptyEnumeration();
        }

        return this.delegate.getResources(name);
    }

}

