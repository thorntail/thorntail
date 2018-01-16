package org.jboss.unimbus.jndi;

import java.util.Hashtable;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

public class InMemoryInitialContextFactoryBuilder implements InitialContextFactoryBuilder {

    public InMemoryInitialContextFactoryBuilder(BeanManager beanManager) {
        this.factory = new InMemoryContextFactory(beanManager);
    }

    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
        return this.factory;
    }

    private final InMemoryContextFactory factory;
}
