package io.thorntail.servlet.impl.undertow.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.undertow.Undertow;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.thorntail.servlet.annotation.Primary;

/**
 * Created by bob on 1/17/18.
 */
@Primary
@ApplicationScoped
public class PrimaryHTTPListenerConfigurer implements UndertowConfigurer {

    @Override
    public void configure(Undertow.Builder builder) {
        if (this.port >= 0) {
            builder.addHttpListener(this.port, this.host);
        }
    }

    @Inject
    @ConfigProperty(name = "web.primary.port")
    private int port;

    @Inject
    @ConfigProperty(name = "web.primary.host")
    private String host;
}
