package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public class SimpleSubsystemDefaulter<T extends Subsystem> extends AbstractSubsystemDefaulter<T> {

    public SimpleSubsystemDefaulter(Class<T> subsystemClass) {
        super(subsystemClass);
    }

    @Override
    public T getDefaultSubsystem() throws Exception {
        return getSubsystemType().newInstance();
    }
}
