package io.thorntail.tracing.jaeger.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.senders.Sender;
import com.uber.jaeger.senders.UdpSender;
import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.tracing.jaeger.Http;
import io.thorntail.tracing.jaeger.Udp;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("com.uber.jaeger.senders.Sender")
public class SenderProducer {

    @Produces
    @Singleton
    Sender sender() {
        if (this.endpoint.isPresent()) {
            return this.httpSenderProvider.get();
        }
        return this.udpSenderProvider.get();
    }

    @Inject
    @ConfigProperty(name = "jaeger.endpoint")
    Optional<String> endpoint;

    @Inject
    @Udp
    Instance<UdpSender> udpSenderProvider;

    @Inject
    @Http
    Instance<HttpSender> httpSenderProvider;
}
