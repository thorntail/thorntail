package org.wildfly.swarm.remoting;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.server.HTTPListener;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Post;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Ken Finnigan
 */
@Post
@ApplicationScoped
public class EnableListener implements Customizer {

    @Inject
    @Any
    Instance<Undertow> undertow;

    @Override
    public void customize() {
        System.setProperty(SwarmProperties.HTTP_EAGER, "true");

        if (!undertow.isUnsatisfied()) {
            Server server = undertow.get().subresources().server("default-server");
            if (server != null) {
                HTTPListener listener = server.subresources().httpListener("default");
                if (listener != null) {
                    listener.enabled(true);
                }
            }
        }
    }
}
