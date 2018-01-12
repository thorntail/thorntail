package org.jboss.unimbus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
class InitializerHandler {

    @Inject
    @Any
    private Instance<Initializer> initializers;

    void pre() {
        System.err.println("FIRING INITIALIZRS - PRE");
        for (Initializer each : this.initializers) {
            System.err.println("firing: " + each);
            each.preInitialize();
        }
    }

    void post() {
        System.err.println("FIRING INITIALIZRS - POST");
        for (Initializer each : this.initializers) {
            System.err.println("firing: " + each);
            each.postInitialize();
        }
    }
}
