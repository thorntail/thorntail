package org.wildfly.swarm.bootstrap.logging;

/**
 * @author Bob McWhirter
 */
public interface BackingLoggerManager {
    BackingLogger getBackingLogger(String name);
}
