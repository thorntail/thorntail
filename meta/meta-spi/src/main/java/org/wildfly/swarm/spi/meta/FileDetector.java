package org.wildfly.swarm.spi.meta;

/**
 * @author Heiko Braun
 * @since 09/03/2017
 */
public abstract class FileDetector implements FractionDetector<PathSource> {
    @Override
    public abstract void detect(PathSource fileSource);
}
