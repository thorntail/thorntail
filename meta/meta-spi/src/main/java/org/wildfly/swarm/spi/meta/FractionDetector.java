package org.wildfly.swarm.spi.meta;

/**
 * @author Ken Finnigan
 */
public interface FractionDetector<T> {
    default boolean wasDetected() {
        return false;
    }

    default boolean detectionComplete() {
        return false;
    }

    String artifactId();

    String extensionToDetect();

    void detect(T element);
}
