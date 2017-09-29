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

import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@RunWith(Arquillian.class)
public class MeterTest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private Meter injectedMeter;

    @Inject
    private MetricRegistry registry;

    @Test
    @InSequence(1)
    public void testCount() throws Exception {
        // test mark()
        long countBefore = injectedMeter.getCount();
        injectedMeter.mark();
        long countAfter = injectedMeter.getCount();
        Assert.assertEquals(countBefore + 1, countAfter);

        // test mark(2)
        countBefore = injectedMeter.getCount();
        injectedMeter.mark(2);
        countAfter = injectedMeter.getCount();
        Assert.assertEquals(countBefore + 2, countAfter);

        // test mark(-3)
        countBefore = injectedMeter.getCount();
        injectedMeter.mark(-3);
        countAfter = injectedMeter.getCount();
        Assert.assertEquals(countBefore - 3, countAfter);
    }

    private void verifyMeanRate(Meter meter, double beforeStartTime, double afterStartTime, int count) {
        double beforeUptime = System.nanoTime() - afterStartTime;
        double rate = meter.getMeanRate() / TimeUnit.SECONDS.toNanos(1);
        double afterUptime = System.nanoTime() - beforeStartTime;

        double delta = (count / (beforeUptime)) - (count / (afterUptime));
        // System.out.println("DEBUG: (count / (beforeUptime)) " + (count /
        // (beforeUptime)));
        // System.out.println("DEBUG: (count / (afterUptime) " + (count /
        // (afterUptime)));
        // System.out.println("DEBUG: beforeStartTime " + beforeStartTime);
        // System.out.println("DEBUG: afterStartTime " + afterStartTime);
        // System.out.println("DEBUG: beforeUptime " + beforeUptime);
        // System.out.println("DEBUG: afterUptime " + afterUptime);
        // System.out.println("DEBUG: rate " + rate);
        // System.out.println("DEBUG: test " + delta);
        Assert.assertEquals(count / (beforeUptime), rate, delta);
    }

    @Test
    public void testRates() throws Exception {

        /* testMeterRates1 */
        int count = 9999;
        double beforeStartTime = System.nanoTime();
        Meter meter = registry.meter("testMeterRates1");
        double afterStartTime = System.nanoTime();
        meter.mark(count);
        Thread.sleep(10000); // Needs to be greater than the tick interval time.
        verifyMeanRate(meter, beforeStartTime, afterStartTime, count);
        Assert.assertEquals(1839.904, meter.getOneMinuteRate(), 0.001);
        Assert.assertEquals(1966.746, meter.getFiveMinuteRate(), 0.001);
        Assert.assertEquals(1988.720, meter.getFifteenMinuteRate(), 0.001);

        /* testMeterRates2 */
        count = 1;
        beforeStartTime = System.nanoTime();
        meter = registry.meter("testMeterRates2");
        afterStartTime = System.nanoTime();
        meter.mark();
        Thread.sleep(10000); // Needs to be greater than the tick interval time.
        verifyMeanRate(meter, beforeStartTime, afterStartTime, count);
        Assert.assertEquals(0.184, meter.getOneMinuteRate(), 0.001);
        Assert.assertEquals(0.196, meter.getFiveMinuteRate(), 0.001);
        Assert.assertEquals(0.198, meter.getFifteenMinuteRate(), 0.001);

        /* testMeterRates3 */
        count = 2000000000;
        beforeStartTime = System.nanoTime();
        meter = registry.meter("testMeterRates3");
        afterStartTime = System.nanoTime();
        meter.mark(count);
        Thread.sleep(10000); // Needs to be greater than the tick interval time.
        verifyMeanRate(meter, beforeStartTime, afterStartTime, count);
        Assert.assertEquals(368017765.851, meter.getOneMinuteRate(), 0.001);
        Assert.assertEquals(393388581.528, meter.getFiveMinuteRate(), 0.001);
        Assert.assertEquals(397783939.201, meter.getFifteenMinuteRate(), 0.001);
    }
}
