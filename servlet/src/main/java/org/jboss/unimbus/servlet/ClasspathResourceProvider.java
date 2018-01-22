package org.jboss.unimbus.servlet;

import java.net.URL;

/**
 * Created by bob on 1/22/18.
 */
public class ClasspathResourceProvider implements ResourceProvider {

    public ClasspathResourceProvider(String prefix) {
        if ( prefix.startsWith("/" ) ) {
            prefix = prefix.substring(1);
        }
        this.prefix = prefix;
    }

    @Override
    public URL getResource(String path) {
        String search = this.prefix + path;
        return getClass().getClassLoader().getResource(search);
    }

    private final String prefix;
}
