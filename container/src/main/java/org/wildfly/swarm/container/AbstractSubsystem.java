package org.wildfly.swarm.container;

import org.jboss.dmr.ModelNode;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSubsystem implements Subsystem {

    private int priority = 0;

    public AbstractSubsystem() {

    }

    public AbstractSubsystem(int priority) {
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
