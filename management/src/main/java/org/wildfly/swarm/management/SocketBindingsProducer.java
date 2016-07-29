package org.wildfly.swarm.management;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class SocketBindingsProducer {

    @Produces
    public SocketBinding managementHttpSocketBinding() {
        return new SocketBinding("management-http")
                .port(SwarmProperties.propertyVar(ManagementProperties.HTTP_PORT, "9990"));
    }

    @Produces
    public SocketBinding managementHttpsSocketBinding() {
        return new SocketBinding("management-https")
                .port(SwarmProperties.propertyVar(ManagementProperties.HTTPS_PORT, "9993"));
    }
}
