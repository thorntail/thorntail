package org.wildfly.swarm.tools.exec;

import java.util.Collections;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class MainClass implements Executable {

    private final String className;

    public MainClass(String className) {
        this.className = className;
    }

    @Override
    public List<? extends String> toArguments() {
        return Collections.singletonList( this.className );
    }
}
