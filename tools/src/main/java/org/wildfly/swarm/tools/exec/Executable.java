package org.wildfly.swarm.tools.exec;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface Executable {
    List<? extends String> toArguments();
}
