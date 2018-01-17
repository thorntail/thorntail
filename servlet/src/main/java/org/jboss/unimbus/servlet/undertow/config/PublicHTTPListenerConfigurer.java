package org.jboss.unimbus.servlet.undertow.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.undertow.Undertow;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.annotations.Public;

/**
 * Created by bob on 1/17/18.
 */
@Public
@ApplicationScoped
public class PublicHTTPListenerConfigurer implements UndertowConfigurer {

    @Override
    public void configure(Undertow.Builder builder) {
        System.err.println( "configure public: " + this.host + ":" + this.port);
        builder.addHttpListener( this.port, this.host );
    }

    @Inject
    @ConfigProperty(name="web.public.port")
    private int port;

    @Inject
    @ConfigProperty(name="web.public.host")
    private String host;
}
