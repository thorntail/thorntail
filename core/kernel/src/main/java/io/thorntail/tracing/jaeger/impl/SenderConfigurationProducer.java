package io.thorntail.tracing.jaeger.impl;

import io.jaegertracing.Configuration.SenderConfiguration;
import io.jaegertracing.thrift.internal.senders.UdpSender;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.thorntail.condition.annotation.RequiredClassPresent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("io.jaegertracing.Configuration")
public class SenderConfigurationProducer {

    @Produces
    @Singleton
    public SenderConfiguration get() {
        return new SenderConfiguration()
            .withEndpoint(this.endpoint.orElse("http://localhost:14268/api/traces"))
            .withAgentHost(this.agentHost.orElse(UdpSender.DEFAULT_AGENT_UDP_HOST))
            .withAgentPort(this.agentPort.orElse(UdpSender.DEFAULT_AGENT_UDP_COMPACT_PORT))
            .withAuthUsername(this.authUser.orElse(null))
            .withAuthPassword(this.authPassword.orElse(null))
            .withAuthToken(this.authToken.orElse(null));
    }

    @Inject
    @ConfigProperty(name = "jaeger.endpoint")
    Optional<String> endpoint;

    @Inject
    @ConfigProperty(name = "jaeger.agent.host")
    Optional<String> agentHost;

    @Inject
    @ConfigProperty(name = "jaeger.agent.port")
    Optional<Integer> agentPort;

    @Inject
    @ConfigProperty(name = "jaeger.auth.user")
    Optional<String> authUser;

    @Inject
    @ConfigProperty(name = "jaeger.auth.password")
    Optional<String> authPassword;

    @Inject
    @ConfigProperty(name = "jaeger.auth.token")
    Optional<String> authToken;
}
