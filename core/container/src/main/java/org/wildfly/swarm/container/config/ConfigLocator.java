package org.wildfly.swarm.container.config;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public abstract class ConfigLocator {

    public static final String PROJECT_PREFIX = "project-";

    public ConfigLocator() {
    }

    public abstract Stream<URL> locate(String profileName) throws IOException;

}
