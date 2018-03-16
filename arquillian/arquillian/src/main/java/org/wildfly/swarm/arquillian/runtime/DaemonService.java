package org.wildfly.swarm.arquillian.runtime;

import java.net.BindException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Vetoed;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.swarm.arquillian.daemon.server.Server;
import org.wildfly.swarm.arquillian.daemon.server.ServerLifecycleException;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class DaemonService implements Service<Server> {

    public static final ServiceName SERVICE_NAME = ServiceName.of("wildfly", "swarm", "arquillian", "daemon");

    private static final Logger log = Logger.getLogger(DaemonService.class.getName());

    @Override
    public void start(StartContext context) throws StartException {
        int port = Integer.getInteger(SwarmProperties.ARQUILLIAN_DAEMON_PORT, 12345);

        try {
            this.server = Server.create("localhost", port);
            this.server.start();
        } catch (Exception e) {
            // this shouldn't be possible per Java control flow rules, but there is a "sneaky throw" somewhere
            //noinspection ConstantConditions
            if (e instanceof BindException) {
                log.log(Level.SEVERE, "Couldn't bind Arquillian Daemon on localhost:" + port
                        + "; you can change the port using system property '"
                        + SwarmProperties.ARQUILLIAN_DAEMON_PORT + "'", e);
            }

            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.server.stop();
        } catch (ServerLifecycleException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Server getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    static void addService(ServiceTarget serviceTarget) {

        DaemonService daemon = new DaemonService();
        serviceTarget
                .addService(SERVICE_NAME, daemon)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }


    private Server server;
}
