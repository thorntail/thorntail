package io.thorntail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by bob on 2/20/18.
 */
class ServiceRegistryClassLoader extends ClassLoader implements ServiceRegistry {

    ServiceRegistryClassLoader(ClassLoader delegate) {
        this.delegate = delegate;
    }

    void setDelegate(ClassLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return delegate.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        if (this.entries.containsKey(name)) {
            try {
                return this.entries.get(name).getURL();
            } catch (IOException e) {
                // ignore, delegate
            }

        }
        return delegate.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (this.entries.containsKey(name)) {
            List<URL> urls = new ArrayList<>();
            urls.add( this.entries.get(name).getURL() );
            Enumeration<URL> resources = this.delegate.getResources(name);
            while ( resources.hasMoreElements() ) {
                urls.add( resources.nextElement() );
            }
            return Collections.enumeration(urls);
        }
        return delegate.getResources(name);
    }

    public static boolean registerAsParallelCapable() {
        return ClassLoader.registerAsParallelCapable();
    }

    public static URL getSystemResource(String name) {
        return ClassLoader.getSystemResource(name);
    }

    public static Enumeration<URL> getSystemResources(String name) throws IOException {
        return ClassLoader.getSystemResources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (this.entries.containsKey(name)) {
            return this.entries.get(name).openStream();
        }
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

    @Override
    public <T> void register(Class<T> serviceInterface, Class<? extends T> implementationClass) {
        Entry entry = this.entries.get(resourceLocation(serviceInterface));
        if (entry == null) {
            entry = new Entry();
            this.entries.put(resourceLocation(serviceInterface), entry);
        }
        entry.addImplementation( implementationClass );
    }

    private String resourceLocation(Class<?> serviceInterface) {
        return "META-INF/services/" + serviceInterface.getName();
    }

    private ClassLoader delegate;

    private final Map<String, Entry> entries = new HashMap<>();

    private static class Entry {

        Entry() {

        }

        void addImplementation(Class<?> implementationClass) {
            this.implementations.add(implementationClass.getName());
            if (this.file != null) {
                this.file.delete();
                this.file = null;
            }
        }

        InputStream openStream() {
            return new ByteArrayInputStream(this.implementations.stream().collect(Collectors.joining("\n")).getBytes());
        }

        URL getURL() throws IOException {
            if (this.file == null) {
                this.file = File.createTempFile("services", ".tmp");
                this.file.deleteOnExit();
            }

            try (FileOutputStream out = new FileOutputStream(this.file)) {
                out.write(this.implementations.stream().collect(Collectors.joining("\n")).getBytes());
            }

            return this.file.toURI().toURL();
        }

        private File file;

        private final List<String> implementations = new ArrayList<>();


    }
}
