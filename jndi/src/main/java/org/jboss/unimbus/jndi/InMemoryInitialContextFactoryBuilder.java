package org.jboss.unimbus.jndi;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

public class InMemoryInitialContextFactoryBuilder implements InitialContextFactoryBuilder {

    public InMemoryInitialContextFactoryBuilder() {
        this.factory = new InMemoryContextFactory();
    }

    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
        return this.factory;
    }

    private final InMemoryContextFactory factory;
}
