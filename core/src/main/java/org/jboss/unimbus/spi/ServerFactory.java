package org.jboss.unimbus.spi;

/**
 * @author Ken Finnigan
 */
public interface ServerFactory {
    void configure();

    void tearDown();
}
