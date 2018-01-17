package org.jboss.unimbus.servlet.undertow.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.undertow.Undertow;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.annotations.Management;

/**
 * Created by bob on 1/17/18.
 */
@Management
@ApplicationScoped
public class ManagementHTTPListenerConfigurer implements UndertowConfigurer {

    @Override
    public void configure(Undertow.Builder builder) {
        System.err.println( "configure management: " + this.host + ":" + this.port);
        builder.addHttpListener( this.port, this.host );
    }

    @Inject
    @ConfigProperty(name="web.management.port")
    private int port;

    @Inject
    @ConfigProperty(name="web.management.host")
    private String host;
}
