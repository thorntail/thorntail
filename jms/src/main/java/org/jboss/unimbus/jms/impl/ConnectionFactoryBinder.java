package org.jboss.unimbus.jms.impl;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.unimbus.events.LifecycleEvent;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
@Vetoed
public class ConnectionFactoryBinder {

    /*
    void bind(@Observes LifecycleEvent.BeforeStart event) throws NamingException {
        this.context.bind( "java:comp/DefaultConnectionFactory", this.connectionFactory);
    }
    */

    @PreDestroy
    void unbind() {
        try {
            this.context.unbind( "java:/comp/DefaultConnectionFactory");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject
    InitialContext context;

    @Inject
    ConnectionFactory connectionFactory;
}
