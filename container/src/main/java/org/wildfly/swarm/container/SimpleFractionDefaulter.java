package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public class SimpleFractionDefaulter<T extends Fraction> extends AbstractFractionDefaulter<T> {

    public SimpleFractionDefaulter(Class<T> subsystemClass) {
        super(subsystemClass);
    }

    @Override
    public T getDefaultSubsystem() throws Exception {
        return getSubsystemType().newInstance();
    }
}
