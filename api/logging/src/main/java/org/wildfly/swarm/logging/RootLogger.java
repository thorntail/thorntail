package org.wildfly.swarm.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class RootLogger {
    private final List<String> handlers = new ArrayList<>();

    private final String level;

    public RootLogger(String level, String...handlers) {
        this.level = level;
        for ( int i = 0 ; i < handlers.length ; ++i ) {
            this.handlers.add( handlers[i] );
        }
    }

    public List<String> getHandlers() {
        return this.handlers;
    }

    public String getLevel() {
        return this.level;
    }
}
