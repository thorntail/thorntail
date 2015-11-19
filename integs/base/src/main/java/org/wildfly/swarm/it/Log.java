package org.wildfly.swarm.it;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class Log {

    private final List<String> lines;

    public Log(List<String> lines) {
        this.lines = lines;
    }

    public List<String> getLines() {
        return this.lines;
    }
}
