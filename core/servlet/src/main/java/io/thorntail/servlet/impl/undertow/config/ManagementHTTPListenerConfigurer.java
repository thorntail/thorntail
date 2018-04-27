package io.thorntail.servlet.impl.undertow.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.thorntail.servlet.annotation.Management;
import io.undertow.Undertow;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 1/17/18.
 */
@Management
@ApplicationScoped
public class ManagementHTTPListenerConfigurer implements UndertowConfigurer {

    @Override
    public void configure(Undertow.Builder builder) {
        if (this.port >= 0) {
            builder.addHttpListener(this.port, this.host);
        }
    }

    @Inject
    @ConfigProperty(name = "web.management.port")
    private int port;

    @Inject
    @ConfigProperty(name = "web.management.host")
    private String host;
}
