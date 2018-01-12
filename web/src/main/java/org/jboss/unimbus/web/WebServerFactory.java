package org.jboss.unimbus.web;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.unimbus.servlet.spi.WebServer;
import org.jboss.unimbus.spi.ServerFactory;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class WebServerFactory implements ServerFactory {

    @Any
    @Inject
    private Instance<WebServer> webServerInstances;

    public void configure() {
        if (webServerInstances.isAmbiguous()) {
            //TODO: Change to actual logging
            System.out.println("Multiple web server implementations found on the classpath, please choose one!");
            return;
        }

        if (webServerInstances.isResolvable()) {
            webServerInstances.get().start();
        }
    }

    @PreDestroy
    public void tearDown() {
        if (webServerInstances.isResolvable()) {
            webServerInstances.get().stop();
        }
    }
}
