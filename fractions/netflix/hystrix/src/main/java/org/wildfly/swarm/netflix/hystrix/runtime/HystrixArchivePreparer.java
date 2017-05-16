package org.wildfly.swarm.netflix.hystrix.runtime;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.netflix.hystrix.HystrixFraction;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Ken Finnigan
 */
@DeploymentScoped
public class HystrixArchivePreparer implements DeploymentProcessor {

    private final Archive archive;

    @Inject
    HystrixFraction fraction;

    @Inject
    public HystrixArchivePreparer(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() {
        // Add Hystrix Metrix Stream Servlet
        archive.as(WARArchive.class)
                .addServlet("HystrixMetricsStreamServlet", "com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet")
                .withDisplayName("HystrixMetricsStreamServlet")
                .withUrlPattern(this.fraction.streamPath());
    }
}
