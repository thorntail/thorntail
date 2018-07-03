package io.thorntail.metrics.impl;

import io.smallrye.metrics.setup.JmxRegistrar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import java.io.IOException;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 6/29/18
 */
@ApplicationScoped
public class MetricsInitializer {
    void init(@Observes @Initialized(ApplicationScoped.class) Object ignored) throws IOException {
        JmxRegistrar registrar = new JmxRegistrar();
        registrar.init();
    }
}
