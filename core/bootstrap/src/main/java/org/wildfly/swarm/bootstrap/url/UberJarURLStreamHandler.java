package org.wildfly.swarm.bootstrap.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.jboss.modules.Resource;
import org.wildfly.swarm.bootstrap.modules.InMemoryJarResourceLoader;

public class UberJarURLStreamHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        String host = url.getHost();
        String path = url.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        InMemoryJarResourceLoader loader = InMemoryJarResourceLoader.LOADERS.get(host);
        if (loader != null) {
            Resource resource = loader.getResource(path);
            if (resource != null) {
                return new URLConnection(resource.getURL()) {
                    @Override
                    public void connect() throws IOException {
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return resource.openStream();
                    }

                };
            }
        }
        return null;
    }

}
