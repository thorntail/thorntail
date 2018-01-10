package org.jboss.unimbus.undertow;

import javax.enterprise.context.ApplicationScoped;

import io.undertow.Undertow;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class UndertowServer {

    private Undertow undertow;

    private Undertow get() {
        if (undertow == null) {
            //TODO Add stuff to configure the builder before it's built. Inject some config to handle it?
            undertow = Undertow.builder()
                    .addHttpListener(8080, "localhost")
                    .build();
        }

        return undertow;
    }

    public void addServlet() {
        
    }

    public void start() {
        get().start();
    }
}
