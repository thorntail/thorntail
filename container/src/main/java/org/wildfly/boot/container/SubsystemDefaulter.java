package org.wildfly.boot.container;

/**
 * @author Bob McWhirter
 */
public interface SubsystemDefaulter<T extends Subsystem> {

    Class<T> getSubsystemType();
    T getDefaultSubsystem() throws Exception;

}
