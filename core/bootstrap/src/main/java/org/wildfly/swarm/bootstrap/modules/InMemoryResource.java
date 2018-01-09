package org.wildfly.swarm.bootstrap.modules;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.modules.Resource;

public class InMemoryResource implements Resource {

    private final URL url;
    private final String name;
    private final byte[] content;

    public InMemoryResource(URL url, String name, byte[] content) {
        this.url = url;
        this.name = name;
        this.content = content;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public URL getURL() {
        return this.url;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    public String toString() {
        return "In-memory:\n" + new String(this.content) + "\n";
    }
}
