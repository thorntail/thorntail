package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractFraction implements Fraction {

    private int priority = 0;

    public AbstractFraction() {

    }

    public AbstractFraction(int priority) {
        this.priority = priority;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

}
