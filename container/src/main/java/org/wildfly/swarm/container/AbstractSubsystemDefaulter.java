package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSubsystemDefaulter<T extends Subsystem> implements SubsystemDefaulter<T> {

    private final Class<T> subsystemClass;

    public AbstractSubsystemDefaulter(Class<T> subsystemClass) {
        this.subsystemClass = subsystemClass;
    }

    @Override
    public Class<T> getSubsystemType() {
        return this.subsystemClass;
    }

}
