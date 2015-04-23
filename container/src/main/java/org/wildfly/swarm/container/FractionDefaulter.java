package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public interface FractionDefaulter<T extends Fraction> {

    Class<T> getSubsystemType();
    T getDefaultSubsystem() throws Exception;

}
