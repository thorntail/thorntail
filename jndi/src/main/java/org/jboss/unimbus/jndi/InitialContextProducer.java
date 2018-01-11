package org.jboss.unimbus.jndi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

@ApplicationScoped
public class InitialContextProducer {

    @PostConstruct
    void init() throws NamingException {
        NamingManager.setInitialContextFactoryBuilder( new InMemoryInitialContextFactoryBuilder() );
        this.context = new InitialContext();
    }

    @Produces
    @ApplicationScoped
    InitialContext initialContext() throws NamingException {
        return this.context;
    }

    private InitialContext context;
}
