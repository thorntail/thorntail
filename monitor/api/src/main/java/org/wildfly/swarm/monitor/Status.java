package org.wildfly.swarm.monitor;

import java.util.Optional;

/**
 * @author Heiko Braun
 * @since 23/03/16
 */
public interface Status {

    enum State {UP,DOWN}

    State getState();

    Optional<String> getMessage();

}
