package org.wildfly.swarm.bootstrap.logging;

/**
 * @author Bob McWhirter
 */
public interface BackingLogger {

    Object getLevel();

    boolean isDebugEnabled();
    boolean isTraceEnabled();

    void trace(Object message);
    void debug(Object message);
    void info(Object message);

    void warn(Object message);
    void error(Object message);
    void error(Object message, Throwable t);

}
