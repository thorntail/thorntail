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
    void init() {
        try {
            NamingManager.setInitialContextFactoryBuilder(new InMemoryInitialContextFactoryBuilder());
            this.context = new InitialContext();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Produces
    @ApplicationScoped
    InitialContext initialContext() throws NamingException {
        return this.context;
    }

    private InitialContext context;
}
