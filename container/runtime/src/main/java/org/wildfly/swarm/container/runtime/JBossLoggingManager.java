package org.wildfly.swarm.container.runtime;

import org.jboss.logging.Logger;
import org.wildfly.swarm.bootstrap.logging.BackingLogger;
import org.wildfly.swarm.bootstrap.logging.BackingLoggerManager;

/**
 * @author Bob McWhirter
 */
public class JBossLoggingManager implements BackingLoggerManager {

    public JBossLoggingManager() {
    }

    @Override
    public BackingLogger getBackingLogger(String name) {
        return new JBossLoggingLogger(Logger.getLogger(name));
    }
}
