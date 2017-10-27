/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.eclipse.microprofile.metrics.tck;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(Arquillian.class)
public class GaugeTest {

    @Inject
    private MetricRegistry metrics;

    private final AtomicInteger value = new AtomicInteger(0);

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testManualGauge() {
        Assert.assertNull(metrics.getGauges().get("tck.gaugetest.gaugemanual"));
        gaugeMe();

        Assert.assertEquals(0, (metrics.getGauges().get("tck.gaugetest.gaugemanual").getValue()));
        Assert.assertEquals(1, (metrics.getGauges().get("tck.gaugetest.gaugemanual").getValue()));
    }

    public void gaugeMe() {
        @SuppressWarnings("unchecked")
        Gauge<Integer> gaugeManual = metrics.getGauges().get("tck.gaugetest.gaugemanual");
        if (gaugeManual == null) {
            gaugeManual = value::getAndIncrement;
            metrics.register("tck.gaugetest.gaugemanual", gaugeManual);
        }
    }

}
