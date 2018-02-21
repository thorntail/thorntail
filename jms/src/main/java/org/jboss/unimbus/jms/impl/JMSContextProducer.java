package org.jboss.unimbus.jms.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;

import org.jboss.unimbus.jms.JMSContextWrapper;
import org.jboss.unimbus.jms.WrappedJMSContext;
import org.jboss.unimbus.jms.impl.container.ContainerManagedJMSContext;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
public class JMSContextProducer {

    @Produces
    JMSContext jmsContext() {
        return wrap(new ContainerManagedJMSContext(this.connectionFactory.createContext()));
    }

    void dispose(@Disposes JMSContext context) {
        ((WrappedJMSContext) context).getCoreJMSContext().close();
    }

    WrappedJMSContext wrap(WrappedJMSContext context) {
        for (JMSContextWrapper wrapper : this.wrappers) {
            context = wrapper.wrap(context);
        }
        return context;
    }

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    @Any
    Instance<JMSContextWrapper> wrappers;
}
