package org.jboss.unimbus.jms.artemis;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class EmbeddedServerConfigProducer {

    @Produces
    @ApplicationScoped
    Configuration config() {
        // no-op, not used, to just silence warnings.
        return new ConfigurationImpl();
    }
}
