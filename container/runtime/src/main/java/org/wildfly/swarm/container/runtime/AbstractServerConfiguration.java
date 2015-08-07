package org.wildfly.swarm.container.runtime;

import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractServerConfiguration<T extends Fraction> implements ServerConfiguration<T> {

    private final Class<T> type;

    public AbstractServerConfiguration(Class<T> type) {
        this.type = type;
    }

    @Override
    public Class<T> getType() {
        return this.type;
    }

}
