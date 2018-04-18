package io.thorntail.jms.impl.opentracing;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.JMSContext;

import io.thorntail.jms.JMSContextWrapper;
import io.thorntail.jms.WrappedJMSContext;

/**
 * Created by bob on 2/21/18.
 */
@ApplicationScoped
public class TracedJMSContextWrapper implements JMSContextWrapper {
    @Override
    public WrappedJMSContext wrap(JMSContext delegate) {
        return new TracedJMSContext(delegate);
    }
}
