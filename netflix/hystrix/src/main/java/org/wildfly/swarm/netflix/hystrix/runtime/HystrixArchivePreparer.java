package org.wildfly.swarm.netflix.hystrix.runtime;

import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.netflix.hystrix.HystrixProperties;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Ken Finnigan
 */
@Singleton
public class HystrixArchivePreparer implements ArchivePreparer {
    @Override
    public void prepareArchive(Archive<?> archive) {
        // Add Hystrix Metrix Stream Servlet
        archive.as(WARArchive.class)
                .addServlet("HystrixMetricsStreamServlet", "com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet")
                .withDisplayName("HystrixMetricsStreamServlet")
                .withUrlPattern(System.getProperty(HystrixProperties.HYSTRIX_STREAM_PATH, "/hystrix.stream"));
    }
}
