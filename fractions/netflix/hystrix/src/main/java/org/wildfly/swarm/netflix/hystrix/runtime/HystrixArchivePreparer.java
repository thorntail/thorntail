package org.wildfly.swarm.netflix.hystrix.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.netflix.hystrix.HystrixFraction;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class HystrixArchivePreparer implements ArchivePreparer {
    @Inject
    HystrixFraction fraction;

    @Override
    public void prepareArchive(Archive<?> archive) {
        // Add Hystrix Metrix Stream Servlet
        archive.as(WARArchive.class)
                .addServlet("HystrixMetricsStreamServlet", "com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet")
                .withDisplayName("HystrixMetricsStreamServlet")
                .withUrlPattern(this.fraction.streamPath());
    }
}
