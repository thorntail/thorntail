package org.wildfly.swarm.bootstrap.modules;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.jboss.modules.ClassSpec;
import org.jboss.modules.PackageSpec;
import org.jboss.modules.Resource;
import org.jboss.modules.ResourceLoader;

public class InstrumentedResourceLoader implements ResourceLoader {

    public InstrumentedResourceLoader(ResourceLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getRootName() {
        return delegate.getRootName();
    }

    @Override
    public ClassSpec getClassSpec(String fileName) throws IOException {
        System.err.println("getClassSpec: " + getRootName() + " // " + fileName);
        ClassSpec result = delegate.getClassSpec(fileName);
        System.err.println(" -- " + result);
        return result;
    }

    @Override
    public PackageSpec getPackageSpec(String name) throws IOException {
        System.err.println("getPackageSpec: " + getRootName() + " // " + name);
        PackageSpec result = delegate.getPackageSpec(name);
        System.err.println(" -- " + result);
        return result;
    }

    @Override
    public Resource getResource(String name) {
        System.err.println("getResource: " + getRootName() + " // " + name);
        Resource result = delegate.getResource(name);
        System.err.println(" -- " + result);
        return result;
    }

    @Override
    public String getLibrary(String name) {
        return delegate.getLibrary(name);
    }

    @Override
    public Collection<String> getPaths() {
        System.err.println("getPaths: " + getRootName());
        Collection<String> paths = delegate.getPaths();
        for (String path : paths) {
            System.err.println(" - " + path);
        }

        return paths;
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public URI getLocation() {
        return delegate.getLocation();
    }

    private final ResourceLoader delegate;

}

