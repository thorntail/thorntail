package org.wildfly.swarm.spi.meta;

/**
 * @author Heiko Braun
 * @since 09/03/2017
 */
public abstract class FileDetector implements FractionDetector<FileSource> {
    @Override
    public abstract void detect(FileSource fileSource);
}
