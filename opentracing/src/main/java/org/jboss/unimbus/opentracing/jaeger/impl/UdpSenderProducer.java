package org.jboss.unimbus.opentracing.jaeger.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Provider;

import com.uber.jaeger.senders.UdpSender;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.opentracing.jaeger.Udp;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("com.uber.jaeger.senders.UdpSender")
public class UdpSenderProducer {

    @Produces
    @ApplicationScoped
    @Udp
    public UdpSender get() {
        return new UdpSender(this.agentHost.orElse(null),
                             this.agentPort.orElse(0),
                             0);
    }

    @ConfigProperty(name = "jaeger.agent.host")
    Optional<String> agentHost;

    @Inject
    @ConfigProperty(name = "jaeger.agent.port")
    Optional<Integer> agentPort;

}
