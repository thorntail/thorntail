package org.wildfly.swarm.container.runtime;

import org.jboss.logging.Logger;
import org.wildfly.swarm.bootstrap.logging.BackingLogger;

/**
 * @author Bob McWhirter
 */
public class JBossLoggingLogger implements BackingLogger {
    private final Logger logger;

    public JBossLoggingLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Object getLevel() {
        if ( this.logger.isEnabled( Logger.Level.TRACE ) ) {
            return "TRACE";
        }
        if ( this.logger.isEnabled( Logger.Level.DEBUG ) ) {
            return "DEBUG";
        }
        if ( this.logger.isEnabled( Logger.Level.INFO ) ) {
            return "INFO";
        }
        if ( this.logger.isEnabled( Logger.Level.WARN ) ) {
            return "WARN";
        }
        if ( this.logger.isEnabled( Logger.Level.ERROR ) ) {
            return "ERROR";
        }

        return "UNKNOWN";
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    @Override
    public void trace(Object message) {
        this.logger.trace( message );
    }

    @Override
    public void debug(Object message) {
        this.logger.debug( message );
    }

    @Override
    public void info(Object message) {
        this.logger.info( message );
    }

    @Override
    public void warn(Object message) {
        this.logger.warn( message );
    }

    @Override
    public void error(Object message) {
        this.logger.error( message );
    }

    @Override
    public void error(Object message, Throwable t) {
        this.logger.error( message, t );
    }
}
