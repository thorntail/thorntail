package org.jboss.unimbus;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class Initializers {

    @PostConstruct
    void init() {
        System.err.println("FIRING INITIALIZRS");
        for (Initializer each : this.initializers) {
            System.err.println("firing: " + each);
            each.initialize();
        }
    }

    @Inject
    @Any
    private Instance<Initializer> initializers;
}
