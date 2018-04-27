package io.thorntail.servlet.impl.undertow.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by bob on 1/24/18.
 */
public class SortingClassLoader extends ClassLoader {

    public SortingClassLoader(ClassLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return delegate.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        return delegate.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> result = delegate.getResources(name);

        if (!name.startsWith("META-INF/services")) {
            return result;
        }
        List<URL> list = new ArrayList<>();
        List<URL> tail = new ArrayList<>();

        while (result.hasMoreElements()) {
            URL each = result.nextElement();
            if (each.getPath().contains("keycloak-undertow-adapter")) {
                tail.add(each);
            } else {
                list.add(each);
            }
        }

        list.addAll(tail);

        return Collections.enumeration(list);
    }

    public static URL getSystemResource(String name) {
        return ClassLoader.getSystemResource(name);
    }

    public static Enumeration<URL> getSystemResources(String name) throws IOException {
        return ClassLoader.getSystemResources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return delegate.getResourceAsStream(name);
    }

    public static InputStream getSystemResourceAsStream(String name) {
        return ClassLoader.getSystemResourceAsStream(name);
    }

    public static ClassLoader getSystemClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        delegate.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        delegate.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        delegate.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        delegate.clearAssertionStatus();
    }

    private final ClassLoader delegate;
}
