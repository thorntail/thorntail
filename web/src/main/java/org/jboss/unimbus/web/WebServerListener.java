package org.jboss.unimbus.web;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.unimbus.events.BeforeStart;
import org.jboss.unimbus.events.Start;
import org.jboss.unimbus.servlet.spi.WebServer;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class WebServerListener {

    @Any
    @Inject
    private Instance<WebServer> webServerInstances;

    public void checkForWebServers(@Observes @BeforeStart Boolean event) {
        if (webServerInstances.isAmbiguous()) {
            //TODO: Change to actual logging
            System.out.println("Multiple web server implementations found on the classpath, please choose one!");
            for (WebServer each : webServerInstances) {
                System.err.println( " --> " + each );
            }
        }
    }

    public void start(@Observes @Start Boolean event) {
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
