package org.jboss.unimbus.config.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

class ClasspathResourcesConfigSource extends MultiConfigSource {

    protected ClasspathResourcesConfigSource(String path, ClassLoader classLoader) {
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL each = resources.nextElement();
                try (InputStream in = each.openStream()) {
                    Properties props = new Properties();
                    props.load(in);
                    addConfigSource(new PropertiesConfigSource(each.toExternalForm(), props));
                }
            }
        } catch (IOException e) {
            // ignore
        }
        if (Thread.currentThread().getContextClassLoader() != classLoader) {
            try {
                Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
                while (resources.hasMoreElements()) {
                    URL each = resources.nextElement();
                    try (InputStream in = each.openStream()) {
                        Properties props = new Properties();
                        props.load(in);
                        addConfigSource(new PropertiesConfigSource(each.toExternalForm(), props));
                    }
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
