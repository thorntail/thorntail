/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.netflix.hystrix.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.netflix.hystrix.HystrixFraction;
import org.wildfly.swarm.netflix.hystrix.HystrixProperties;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Ken Finnigan
 */
public class HystrixConfiguration extends AbstractServerConfiguration<HystrixFraction> {
    public HystrixConfiguration() {
        super(HystrixFraction.class);
    }

    @Override
    public void prepareArchive(Archive<?> archive) {
        archive.as(JARArchive.class).addModule("com.netflix.hystrix");
        archive.as(JARArchive.class).addModule("io.reactivex.rxjava");

        // Add Hystrix Metrix Stream Servlet
        archive.as(WARArchive.class)
                .addServlet("HystrixMetricsStreamServlet", "com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet")
                .withDisplayName("HystrixMetricsStreamServlet")
                .withUrlPattern(System.getProperty(HystrixProperties.HYSTRIX_STREAM_PATH, "/hystrix.stream"));
    }
}
