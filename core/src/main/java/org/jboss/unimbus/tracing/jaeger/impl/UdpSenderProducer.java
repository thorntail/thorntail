package org.jboss.unimbus.tracing.jaeger.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.uber.jaeger.senders.UdpSender;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.tracing.jaeger.Udp;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("com.uber.jaeger.senders.UdpSender")
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
