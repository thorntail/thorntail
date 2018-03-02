package org.jboss.unimbus.tracing.jaeger.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.uber.jaeger.senders.HttpSender;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.tracing.jaeger.Http;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("com.uber.jaeger.senders.HttpSender")
public class HttpSenderProducer {

    @Produces
    @Singleton
    @Http
    public HttpSender get() {
        return new HttpSender(this.endpoint.get());
    }

    @Inject
    @ConfigProperty(name = "jaeger.endpoint")
    Optional<String> endpoint;

}
