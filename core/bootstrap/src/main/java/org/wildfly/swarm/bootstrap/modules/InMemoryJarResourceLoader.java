package org.wildfly.swarm.bootstrap.modules;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.jboss.modules.AbstractResourceLoader;
import org.jboss.modules.ClassSpec;
import org.jboss.modules.PackageSpec;
import org.jboss.modules.Resource;
import org.jboss.modules.maven.ArtifactCoordinates;

public class InMemoryJarResourceLoader extends AbstractResourceLoader {

    public static final Map<String, InMemoryJarResourceLoader> LOADERS = new HashMap<>();

    public InMemoryJarResourceLoader(ArtifactCoordinates coords, InputStream jarStream) throws IOException {
        this(coords.getGroupId().replace('.', '-') + "-" + coords.getArtifactId() + "-" + coords.getVersion(), new JarInputStream(jarStream));
    }

    public InMemoryJarResourceLoader(String rootName, InputStream jarStream) throws IOException {
        this(rootName, new JarInputStream(jarStream));
    }

    public InMemoryJarResourceLoader(String rootName, JarInputStream jarStream) throws IOException {
        this.rootName = rootName;
        this.paths.add("");
        initialize(jarStream);
        synchronized (InMemoryJarResourceLoader.class) {
            LOADERS.put(rootName, this);
        }
    }

    private void initialize(JarInputStream jarStream) throws IOException {
        JarEntry entry;
        while ((entry = jarStream.getNextJarEntry()) != null) {
            if (entry.isDirectory()) {
                // skip
            } else {
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    initializeClassSpec(entry, jarStream);
                } else {
                    initializeResource(entry, jarStream);
                }
            }
        }
    }

    private void initializeClassSpec(JarEntry entry, JarInputStream jarStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] buf = new byte[1024];
        int len = 0;

        while ((len = jarStream.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }

        out.close();

        ClassSpec classSpec = new ClassSpec();
        classSpec.setBytes(out.toByteArray());
        classSpec.setCodeSource(new CodeSource(new URL("uberjar://" + this.rootName + "/"), (Certificate[]) null));
        this.classes.put(entry.getName(), classSpec);
        index(entry.getName());
    }

    private void initializeResource(JarEntry entry, JarInputStream jarStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] buf = new byte[1024];
        int len = 0;

        while ((len = jarStream.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }

        out.close();

        String name = entry.getName();
        String relativeName = name;
        int slashLoc = name.lastIndexOf('/');
        if (slashLoc >= 0) {
            relativeName = name.substring(slashLoc + 1);
        }

        InMemoryResource resource = new InMemoryResource(createUrl(this.rootName, name), relativeName, out.toByteArray());

        this.resources.put(name, resource);
        index(name);
    }

    private URL createUrl(String rootName, String name) throws MalformedURLException {
        return new URL("uberjar://" + rootName + "/" + name);
    }

    private void index(String name) {
        while (true) {
            int slashLoc = name.lastIndexOf('/');
            if (slashLoc < 0) {
                return;
            }

            name = name.substring(0, slashLoc);
            this.paths.add(name);
        }
    }


    @Override
    public String getRootName() {
        return this.rootName;
    }

    @Override
    public ClassSpec getClassSpec(String fileName) throws IOException {
        ClassSpec spec = this.classes.get(fileName);
        return spec;
    }

    @Override
    public PackageSpec getPackageSpec(String name) throws IOException {
        return super.getPackageSpec(name);
    }

    @Override
    public Resource getResource(String name) {
        Resource resource = this.resources.get(name);
        if (resource != null) {
            return resource;
        }

        ClassSpec classSpec = this.classes.get(name);
        if (classSpec != null) {
            try {
                return new InMemoryResource(createUrl(this.rootName, name), name, classSpec.getBytes());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getLibrary(String name) {
        return super.getLibrary(name);
    }

    @Override
    public Collection<String> getPaths() {
        return paths;
    }

    @Override
    public void close() {

    }

    @Override
    public URI getLocation() {
        return null;
    }

    private final String rootName;
    private Map<String, ClassSpec> classes = new HashMap<>();
    private Map<String, Resource> resources = new HashMap<>();
    private Set<String> paths = new HashSet<>();

}

