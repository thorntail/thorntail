package org.jboss.unimbus.jndi;

import java.util.Hashtable;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class InMemoryContextFactory implements InitialContextFactory {
    public InMemoryContextFactory(BeanManager beanManager) {
        this.context = new InMemoryContext(beanManager);
    }

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return this.context;
    }

    private final InMemoryContext context;
}
