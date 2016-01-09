package org.wildfly.swarm.bootstrap.modules;

import org.jboss.modules.maven.MavenResolver;

/**
 * @author Bob McWhirter
 */
public class MavenResolvers {

    private static final MultiMavenResolver INSTANCE = new MultiMavenResolver();

    static {
        INSTANCE.addResolver( new UberJarMavenResolver() );
        INSTANCE.addResolver( MavenResolver.createDefaultResolver() );
    }

    public synchronized static MavenResolver get() {
        return INSTANCE;
    }

}
