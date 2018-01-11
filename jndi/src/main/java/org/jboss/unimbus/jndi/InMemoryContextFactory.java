package org.jboss.unimbus.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class InMemoryContextFactory implements InitialContextFactory {
    public InMemoryContextFactory() {
        this.context = new InMemoryContext((InMemoryContext) null);
    }

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return this.context;
    }

    private final InMemoryContext context;
}
