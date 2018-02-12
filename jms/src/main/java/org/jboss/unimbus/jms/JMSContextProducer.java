package org.jboss.unimbus.jms;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;

import org.jboss.unimbus.jms.impl.WrappedJMSContext;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
public class JMSContextProducer {

    @Produces
    JMSContext jmsContext() {
        return new WrappedJMSContext( this.connectionFactory.createContext() );
    }

    void dispose(@Disposes JMSContext context) {
        ((WrappedJMSContext)context).getDelegate().close();;
    }

    @Inject
    ConnectionFactory connectionFactory;
}
