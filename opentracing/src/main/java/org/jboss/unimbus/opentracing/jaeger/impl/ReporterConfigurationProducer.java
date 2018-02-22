package org.jboss.unimbus.opentracing.jaeger.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

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
    @ApplicationScoped
    Configuration.ReporterConfiguration reporterConfiguration() {
        return new Configuration.ReporterConfiguration(this.sender);
    }

    @Inject
    Sender sender;

}
