package org.wildfly.swarm.bootstrap.logging;

/**
 * @author Bob McWhirter
 */
public class InitialBackingLogger implements BackingLogger {

    private final String category;
    private final BootstrapLogger.Level level;


    public InitialBackingLogger(String category, BootstrapLogger.Level level) {
        this.category = category;
        this.level = level;
    }

    public String getCategory() {
        return this.category;
    }

    public BootstrapLogger.Level getLevel() {
        return this.level;
    }

    @Override
    public boolean isDebugEnabled() {
        return this.level.ordinal() >= BootstrapLogger.Level.DEBUG.ordinal();
    }

    @Override
    public boolean isTraceEnabled() {
        return this.level.ordinal() >= BootstrapLogger.Level.TRACE.ordinal();
    }

    @Override
    public void trace(Object message) {
        InitialLoggerManager.INSTANCE.log( this, BootstrapLogger.Level.TRACE, message );
    }

    @Override
    public void debug(Object message) {
        InitialLoggerManager.INSTANCE.log( this, BootstrapLogger.Level.DEBUG, message );
    }

    @Override
    public void info(Object message) {
        InitialLoggerManager.INSTANCE.log( this, BootstrapLogger.Level.INFO, message );
    }

    @Override
    public void warn(Object message) {
        InitialLoggerManager.INSTANCE.log( this, BootstrapLogger.Level.WARN, message );
    }

    @Override
    public void error(Object message) {
        InitialLoggerManager.INSTANCE.log( this, BootstrapLogger.Level.ERROR, message );
    }

    @Override
    public void error(Object message, Throwable t) {
        InitialLoggerManager.INSTANCE.log( this, BootstrapLogger.Level.ERROR, message, t );
    }
}
