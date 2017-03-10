package org.wildfly.swarm.spi.meta;

/**
 * @author Heiko Braun
 * @since 09/03/2017
 */
public abstract class FileDetector implements FractionDetector<String> {
    @Override
    public abstract void detect(String fileName);
}
