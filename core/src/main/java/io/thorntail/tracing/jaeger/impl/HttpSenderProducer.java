package io.thorntail.tracing.jaeger.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.uber.jaeger.senders.HttpSender;
import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.tracing.jaeger.Http;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
