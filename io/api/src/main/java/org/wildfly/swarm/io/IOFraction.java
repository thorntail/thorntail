package org.wildfly.swarm.io;

import org.wildfly.swarm.config.io.Io;
import org.wildfly.swarm.config.io.subsystem.bufferPool.BufferPool;
import org.wildfly.swarm.config.io.subsystem.worker.Worker;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class IOFraction extends Io<IOFraction> implements Fraction {
    public IOFraction() {
    }

    public static IOFraction createDefaultFraction() {
        return new IOFraction().worker(new Worker("default"))
                .bufferPool(new BufferPool("default"));


    }
}
