package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public interface SubsystemDefaulter<T extends Subsystem> {

    Class<T> getSubsystemType();
    T getDefaultSubsystem() throws Exception;

}
