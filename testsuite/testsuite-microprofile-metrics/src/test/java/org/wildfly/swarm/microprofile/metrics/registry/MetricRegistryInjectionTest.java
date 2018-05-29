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
package org.wildfly.swarm.microprofile.metrics.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Counting;
import org.eclipse.microprofile.metrics.Metric;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.metrics.HelloService;
import org.wildfly.swarm.microprofile.metrics.MetricsSummary;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class MetricRegistryInjectionTest {

    @Inject
    HelloService hello;

    @Inject
    MetricsSummary summary;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addPackage(MetricsSummary.class.getPackage())
                .addPackage(MetricRegistryInjectionTest.class.getPackage());
    }

    @Test
    public void testInjection() {
        hello.hello();
        assertNotNull(summary.getBaseMetrics().getMetrics().containsKey("memory.usedHeap"));
        assertNotNull(summary.getVendorMetrics().getMetrics().containsKey("loadedModules"));
        Metric helloCountMetric = summary.getAppMetrics().getMetrics().get("hello-count");
        assertNotNull(helloCountMetric);
        assertTrue(helloCountMetric instanceof Counting);
        Counting helloCount = (Counting) helloCountMetric;
        assertEquals(1, helloCount.getCount());
    }

}