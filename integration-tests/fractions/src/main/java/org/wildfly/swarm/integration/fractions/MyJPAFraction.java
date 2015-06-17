package org.wildfly.swarm.integration.fractions;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.jpa.JPAFraction;

/**
 * @author Ken Finnigan
 */
public class MyJPAFraction extends JPAFraction {
    @Override
    public void initialize(Container.InitContext initContext) {
        //Do Nothing
    }
}
