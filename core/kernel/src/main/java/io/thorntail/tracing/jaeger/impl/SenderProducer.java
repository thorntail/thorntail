package io.thorntail.tracing.jaeger.impl;

import io.jaegertracing.spi.Sender;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import io.jaegertracing.thrift.internal.senders.UdpSender;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.tracing.jaeger.Http;
import io.thorntail.tracing.jaeger.Udp;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("io.jaegertracing.Configuration")
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
