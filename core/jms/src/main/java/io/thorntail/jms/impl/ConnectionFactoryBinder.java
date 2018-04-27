package io.thorntail.jms.impl;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
@Vetoed
public class ConnectionFactoryBinder {

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
