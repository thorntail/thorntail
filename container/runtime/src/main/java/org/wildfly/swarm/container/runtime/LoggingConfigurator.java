package org.wildfly.swarm.container.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.as.controller.persistence.ConfigurationPersister;
import org.jboss.as.logging.logmanager.ConfigurationPersistence;
import org.jboss.logmanager.Configurator;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.PropertyConfigurator;
import org.jboss.logmanager.config.LogContextConfiguration;
import org.wildfly.swarm.bootstrap.logging.InitialLoggerManager;
import org.wildfly.swarm.bootstrap.logging.LevelNode;

/**
 * @author Bob McWhirter
 */
public class LoggingConfigurator extends ConfigurationPersistence implements Configurator {

    private final LogContext context;

    private final PropertyConfigurator propertyConfigurator;

    /**
     * Construct an instance.
     */
    public LoggingConfigurator() {
        this(LogContext.getSystemLogContext());
    }

    /**
     * Construct a new instance.
     *
     * @param context the log context to be configured
     */
    public LoggingConfigurator(LogContext context) {
        this.context = context;
        this.propertyConfigurator = new PropertyConfigurator(this.context);
    }

    @Override
    public void configure(InputStream inputStream) throws IOException {
        this.propertyConfigurator.configure(inputStream);
        LogContextConfiguration config = this.propertyConfigurator.getLogContextConfiguration();
        config.getHandlerConfiguration( "CONSOLE" ).setLevel( "ALL" );
        LevelNode root = InitialLoggerManager.INSTANCE.getRoot();
        apply( root, config );
        config.commit();
    }

    protected void apply(LevelNode node, LogContextConfiguration config) {
        if ( ! node.getName().equals( "" ) ) {
            config.addLoggerConfiguration( node.getName() ).setLevel( node.getLevel().toString() );
        }

        for (LevelNode each : node.getChildren()) {
            apply( each, config );
        }
    }
}
