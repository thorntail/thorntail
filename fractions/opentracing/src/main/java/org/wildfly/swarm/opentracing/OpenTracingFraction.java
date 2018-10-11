package org.wildfly.swarm.opentracing;

import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

import java.util.Optional;

/**
 * @author Juraci Paixão Kröhling
 */
@DeploymentModule(name = "org.wildfly.swarm.opentracing", slot = "deployment")
@Configurable("thorntail.opentracing")
public class OpenTracingFraction implements Fraction<OpenTracingFraction> {
    @AttributeDocumentation("The servlet skip pattern as a Java compilable Pattern. Optional. Ex.: `/health-check`")
    @Configurable("thorntail.opentracing.servlet.skipPattern")
    private Defaultable<String> servletSkipPattern = Defaultable.string(getDefault(TracingFilter.SKIP_PATTERN));

    public String getServletSkipPattern() {
        return servletSkipPattern.get();
    }

    private static String getDefault(String key) {
        return Optional.ofNullable(System.getProperty(key, System.getenv(key))).orElse("");
    }
}
