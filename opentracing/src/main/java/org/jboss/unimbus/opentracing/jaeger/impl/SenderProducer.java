package org.jboss.unimbus.opentracing.jaeger.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.senders.Sender;
import com.uber.jaeger.senders.UdpSender;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("com.uber.jaeger.senders.Sender")
public class SenderProducer {

    @Produces
    @ApplicationScoped
    Sender sender() {
        if ( this.endpoint.isPresent() ) {
            return this.httpSenderProvider.get();
        }
        return this.udpSenderProvider.get();
    }

    @Inject
    @ConfigProperty(name = "jaeger.endpoint")
    Optional<Boolean> endpoint;

    @Inject
    Instance<UdpSender> udpSenderProvider;

    @Inject
    Instance<HttpSender> httpSenderProvider;
}
