package org.wildfly.swarm.container.runtime;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.logmanager.LogManager;

/**
 * @author Bob McWhirter
 */
public class LogSilencer {

    private Logger logger;
    private Level originalLevel;

    public LogSilencer(String logger) {
        try {
            java.util.logging.LogManager lm = LogManager.getLogManager();
            this.logger = lm.getLogger( "org.jboss.weld" );
            this.originalLevel = this.logger.getLevel();
            this.logger.setLevel(Level.SEVERE);
        } catch (Throwable t) {
            // ignore;
        }
    }

    public void unsilence() {
        if ( this.logger != null ) {
            this.logger.setLevel( this.originalLevel );
        }
    }
}
