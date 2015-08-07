package org.wildfly.swarm.container;

import java.util.Set;

/**
 * @author Bob McWhirter
 */
public interface Server {

    Deployer start(Container config) throws Exception;
    void stop() throws Exception;

    Set<Class<? extends Fraction>> getFractionTypes();
    Fraction createDefaultFor(Class<? extends Fraction> fractionClazz);
}
