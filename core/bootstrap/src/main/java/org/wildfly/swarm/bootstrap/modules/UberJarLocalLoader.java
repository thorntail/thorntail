package org.wildfly.swarm.bootstrap.modules;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.jboss.modules.AbstractLocalLoader;
import org.jboss.modules.Resource;

public class UberJarLocalLoader extends AbstractLocalLoader {
    @Override
    public List<Resource> loadResourceLocal(String name) {
        if (name.equals("META-INF/services/java.net.URLStreamHandlerFactory")) {
            final URL url = UberJarLocalLoader.class.getClassLoader().getResource("java.net.URLStreamHandlerFactory");
            return Collections.singletonList(
                    new Resource() {
                        @Override
                        public String getName() {
                            return url.getPath();
                        }

                        @Override
                        public URL getURL() {
                            return url;
                        }

                        @Override
                        public InputStream openStream() throws IOException {
                            return url.openStream();
                        }

                        @Override
                        public long getSize() {
                            return 0L;
                        }
                    }
            );
        }
        return null;
    }
}
