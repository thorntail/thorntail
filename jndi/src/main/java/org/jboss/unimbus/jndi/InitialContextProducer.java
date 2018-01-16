package org.jboss.unimbus.jndi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.jboss.unimbus.events.LifecycleEvent;

@ApplicationScoped
public class InitialContextProducer {

    public void initialize(@Observes LifecycleEvent.Initialize event) {
        // no-op is fine, but required.
    }

    @PostConstruct
    void init() {
        try {
            NamingManager.setInitialContextFactoryBuilder(
                    new InMemoryInitialContextFactoryBuilder( this.beanManager )
            );
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

    @Inject
    @Any
    Instance<Binder<?>> binders;

    @Inject
    private BeanManager beanManager;
}
