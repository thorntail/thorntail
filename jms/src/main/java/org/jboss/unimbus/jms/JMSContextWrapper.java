package org.jboss.unimbus.jms;

import javax.jms.JMSContext;

/**
 * Interface to allow wrapping of a {@code JMSContext}
 */
public interface JMSContextWrapper {

    WrappedJMSContext wrap(JMSContext delegate);
}
