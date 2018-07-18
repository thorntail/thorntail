package io.thorntail.tracing.jaeger.impl;

import io.jaegertracing.thrift.internal.senders.UdpSender;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.tracing.jaeger.Udp;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("io.jaegertracing.Configuration")
public class UdpSenderProducer {

    @Produces
    @Singleton
    @Udp
    public UdpSender get() {
        return new UdpSender(this.agentHost.orElse(null),
                             this.agentPort.orElse(0),
                             0);
    }

    @Inject
    @ConfigProperty(name = "jaeger.agent.host")
    Optional<String> agentHost;

    @Inject
    @ConfigProperty(name = "jaeger.agent.port")
    Optional<Integer> agentPort;

}
