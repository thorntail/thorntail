package org.wildfly.swarm.logging.runtime;

import java.util.logging.LogManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.config.logging.Logger;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class EarlyLoggingCustomizer implements Customizer {

    @Inject
    @Any
    private LoggingFraction fraction;

    @Override
    public void customize() {
        for (Logger logger : fraction.subresources().loggers()) {
            java.util.logging.Logger l = LogManager.getLogManager().getLogger(logger.getKey());
            l.setLevel(java.util.logging.Level.parse(logger.level().toString()));
        }

    }
}
