/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.restclient.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.restclient.Counter;
import org.wildfly.swarm.microprofile.restclient.HelloResource;
import org.wildfly.swarm.microprofile.restclient.Timer;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class MetricsTest {

    @Inject
    Counter counter;

    @Inject
    Timer timer;

    @ArquillianResource
    URL url;

    @Inject
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    MetricRegistry registry;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addPackage(HelloResource.class.getPackage())
                .addPackage(MetricsTest.class.getPackage());
    }

    @Test
    public void testTimed() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        counter.reset(1);
        timer.reset(0);

        HelloMetricsClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloMetricsClient.class);
        assertEquals("OK1", helloClient.helloTimed());

        org.eclipse.microprofile.metrics.Timer metricsTimer = registry.getTimers().get(HelloMetricsClient.TIMED_NAME);
        assertNotNull(metricsTimer);
        assertEquals(1, metricsTimer.getCount());
    }

    @Test
    public void testCounted() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        counter.reset(1);
        timer.reset(0);

        HelloMetricsClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloMetricsClient.class);
        assertEquals("OK1", helloClient.helloCounted());
        assertEquals("OK2", helloClient.helloCounted());
        assertEquals("OK3", helloClient.helloCounted());

        org.eclipse.microprofile.metrics.Counter metricsCounter = registry.getCounters().get(HelloMetricsClient.COUNTED_NAME);
        assertNotNull(metricsCounter);
        assertEquals(3, metricsCounter.getCount());
    }

    @Test
    public void testClassLevelCounted() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        counter.reset(1);
        timer.reset(0);

        HelloMetricsClassLevelClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloMetricsClassLevelClient.class);
        assertEquals("OK1", helloClient.hello());
        assertEquals("OK2", helloClient.hello());

        org.eclipse.microprofile.metrics.Counter metricsCounter = registry.getCounters().get(HelloMetricsClassLevelClient.class.getName() + "." + "hello");
        assertNotNull(metricsCounter);
        assertEquals(2, metricsCounter.getCount());
    }

}