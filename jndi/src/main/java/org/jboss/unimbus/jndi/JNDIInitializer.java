package org.jboss.unimbus.jndi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;

import org.jboss.unimbus.Initializer;

@ApplicationScoped
public class JNDIInitializer implements Initializer {

    @PostConstruct
    void postConstruct() {
        System.err.println( "##### jndi init");
    }

    @Override
    public void initialize() {
        System.err.println( "**** initialized: " + context);
    }

    @Inject
    private InitialContext context;
}
