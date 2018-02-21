package org.jboss.unimbus.opentracing.impl.jms;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.JMSContext;

import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.jms.JMSContextWrapper;
import org.jboss.unimbus.jms.WrappedJMSContext;

/**
 * Created by bob on 2/21/18.
 */
@ApplicationScoped
@RequiredClassPresent("org.jboss.unimbus.jms.JMSContextWrapper")
public class TracedJMSContextWrapper implements JMSContextWrapper {
    @Override
    public WrappedJMSContext wrap(JMSContext delegate) {
        return new TracedJMSContext(delegate);
    }
}
