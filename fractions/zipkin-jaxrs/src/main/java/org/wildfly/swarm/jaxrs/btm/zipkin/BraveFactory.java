package org.wildfly.swarm.jaxrs.btm.zipkin;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.LoggingReporter;
import com.github.kristofa.brave.Sampler;

/**
 * @author Heiko Braun
 * @since 07/10/16
 */
public class BraveFactory {

    public Brave create() {
        final Brave.Builder builder = new Brave.Builder();
        final Brave brave = builder
                .reporter(new LoggingReporter())
                .traceSampler(Sampler.create(1.0f)) // retain 100% of traces
                .build();
        return brave;
    }
}
