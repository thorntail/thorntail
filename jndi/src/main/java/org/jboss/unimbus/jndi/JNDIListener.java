package org.jboss.unimbus.jndi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.naming.InitialContext;

import org.jboss.unimbus.events.LifecycleEvent;

@ApplicationScoped
public class JNDIListener {

    public void initialize(@Observes LifecycleEvent.Initialize event) {
        System.err.println("**** initialized: " + context);
    }

    @Inject
    private InitialContext context;
}
