package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public interface Fraction {

    default void initialize(Container.InitContext initContext) {
        // Do Nothing
    }

}
