package org.wildfly.swarm.spi.meta;

/**
 * @author Toby Crawley
 * @author Ken Finnigan
 */
public interface SimpleLogger {
    default void debug(String msg) {
    }

    default void info(String msg) {
    }

    default void error(String msg) {
    }

    default void error(String msg, Throwable t) {
    }
}
