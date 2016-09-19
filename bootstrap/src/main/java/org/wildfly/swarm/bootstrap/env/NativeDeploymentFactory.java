package org.wildfly.swarm.bootstrap.env;

import java.io.IOException;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Bob McWhirter
 */
public interface NativeDeploymentFactory {

    Archive nativeDeployment() throws IOException;

    Archive createEmptyArchive(Class<? extends Archive> type, String suffix);

}
