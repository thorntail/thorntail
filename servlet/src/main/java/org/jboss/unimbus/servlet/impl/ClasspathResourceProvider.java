package org.jboss.unimbus.servlet.impl;

import java.net.URL;

import org.jboss.unimbus.servlet.ResourceProvider;

/**
 * Created by bob on 1/22/18.
 */
public class ClasspathResourceProvider implements ResourceProvider {

    public ClasspathResourceProvider(String prefix) {
        if (prefix.startsWith("/")) {
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
