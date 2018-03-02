package org.jboss.unimbus.tracing.jaeger.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.senders.Sender;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("com.uber.jaeger.Configuration")
public class ReporterConfigurationProducer {

    @Produces
    @Singleton
    Configuration.ReporterConfiguration reporterConfiguration() {
        Configuration.ReporterConfiguration config = new Configuration.ReporterConfiguration(this.sender);
        return config;
    }

    @Inject
    Sender sender;

}
