package org.wildfly.swarm.bootstrap.url;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class UberJarURLStreamHandlerFactory implements URLStreamHandlerFactory {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("uberjar")) {
            return new UberJarURLStreamHandler();
        }
        return null;
    }
}
