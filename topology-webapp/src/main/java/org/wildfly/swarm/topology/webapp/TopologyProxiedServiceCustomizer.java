package org.wildfly.swarm.topology.webapp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.config.undertow.HandlerConfiguration;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.configuration.ReverseProxy;
import org.wildfly.swarm.config.undertow.server.Host;
import org.wildfly.swarm.config.undertow.server.host.Location;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.Post;
import org.wildfly.swarm.undertow.UndertowFraction;

import static org.wildfly.swarm.topology.webapp.TopologyWebAppFraction.proxyHandlerName;

/**
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class TopologyProxiedServiceCustomizer implements Customizer {

    @Inject @Any
    private UndertowFraction undertow;

    @Inject @Any
    private TopologyWebAppFraction fraction;

    public void customize() {
        Map<String,String> mappings = this.fraction.proxiedServiceMappings();
        if (!mappings.isEmpty()) {
            HandlerConfiguration handlerConfig = undertow.subresources().handlerConfiguration();
            for (String serviceName : mappings.keySet()) {
                ReverseProxy proxy = new ReverseProxy(proxyHandlerName(serviceName)).hosts(Collections.emptyList());
                handlerConfig.reverseProxy(proxy);

                String contextPath = mappings.get(serviceName);
                for (Server server : undertow.subresources().servers()) {
                    Location location = new Location(contextPath).handler(proxyHandlerName(serviceName));
                    for (Host host : server.subresources().hosts()) {
                        host.location(location);
                    }
                }
            }
        }
    }
}
