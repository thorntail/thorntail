package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractFractionDefaulter<T extends Fraction> implements FractionDefaulter<T> {

    private final Class<T> subsystemClass;

    public AbstractFractionDefaulter(Class<T> subsystemClass) {
        this.subsystemClass = subsystemClass;
    }

    @Override
    public Class<T> getSubsystemType() {
        return this.subsystemClass;
    }

}
