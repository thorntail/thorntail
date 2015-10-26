package org.wildfly.swarm.io;

import org.wildfly.swarm.config.IO;
import org.wildfly.swarm.config.io.BufferPool;
import org.wildfly.swarm.config.io.Worker;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class IOFraction extends IO<IOFraction> implements Fraction {
    public IOFraction() {
    }

    public static IOFraction createDefaultFraction() {
        return new IOFraction().worker(new Worker("default"))
                .bufferPool(new BufferPool("default"));


    }
}
