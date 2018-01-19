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

    public void initialize(@Observes LifecycleEvent.Bootstrap event) {
        // no-op is fine, but required.
    }

    @PostConstruct
    void init() {
        try {
            NamingManager.setInitialContextFactoryBuilder(new InMemoryInitialContextFactoryBuilder());
        } catch (IllegalStateException e) {
            // someone beat us to it.
        } catch (NamingException e) {
            e.printStackTrace();
        }

        try {
            this.context = new InitialContext();
            this.context.bind("java:app/BeanManager", this.beanManager);
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
