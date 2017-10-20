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

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.Timer.Context;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(Arquillian.class)
public class TimerTest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    private static Timer globalTimer = null;

    private static boolean isInitialized = false;

    final static long[] SAMPLE_LONG_DATA = { 0, 10, 20, 20, 20, 30, 30, 30, 30, 30, 40, 50, 50, 60, 70, 70, 70, 80, 90,
            90, 100, 110, 110, 120, 120, 120, 120, 130, 130, 130, 130, 140, 140, 150, 150, 170, 180, 180, 200, 200, 200,
            210, 220, 220, 220, 240, 240, 250, 250, 270, 270, 270, 270, 270, 270, 270, 280, 280, 290, 300, 310, 310,
            320, 320, 330, 330, 360, 360, 360, 360, 370, 380, 380, 380, 390, 400, 400, 410, 420, 420, 420, 430, 440,
            440, 440, 450, 450, 450, 460, 460, 460, 460, 470, 470, 470, 470, 470, 470, 480, 480, 490, 490, 500, 510,
            520, 520, 520, 530, 540, 540, 550, 560, 560, 570, 570, 590, 590, 600, 610, 610, 620, 620, 630, 640, 640,
            640, 650, 660, 660, 660, 670, 670, 680, 680, 700, 710, 710, 710, 710, 720, 720, 720, 720, 730, 730, 740,
            740, 740, 750, 750, 760, 760, 760, 770, 780, 780, 780, 800, 800, 810, 820, 820, 820, 830, 830, 840, 840,
            850, 870, 870, 880, 880, 880, 890, 890, 890, 890, 900, 910, 920, 920, 920, 930, 940, 950, 950, 950, 960,
            960, 960, 960, 970, 970, 970, 970, 980, 980, 980, 990, 990 };

    @Before
    public void initData() {
        if (isInitialized) {
            return;
        }

        globalTimer = registry.timer("test.longData.timer");

        for (long i : SAMPLE_LONG_DATA) {
            globalTimer.update(i, TimeUnit.NANOSECONDS);
        }
        isInitialized = true;
    }

    @Test
    @InSequence(1)
    public void testRates() throws Exception {
        double beforeStartTime = System.nanoTime();
        Timer timer = registry.timer("testRate");
        double afterStartTime = System.nanoTime();

        Context context = timer.time();
        context.stop();
        Thread.sleep(10000);

        double beforeUptime = System.nanoTime() - afterStartTime;
        double mean = timer.getMeanRate() / TimeUnit.SECONDS.toNanos(1);
        double afterUptime = System.nanoTime() - beforeStartTime;

        double delta = (1 / (beforeUptime)) - (1 / (afterUptime));
        // System.out.println("DEBUG: (count / (beforeUptime)) " + (1 /
        // (beforeUptime)));
        // System.out.println("DEBUG: (count / (afterUptime) " + (1 /
        // (afterUptime)));
        // System.out.println("DEBUG: beforeStartTime " + beforeStartTime);
        // System.out.println("DEBUG: afterStartTime " + afterStartTime);
        // System.out.println("DEBUG: beforeUptime " + beforeUptime);
        // System.out.println("DEBUG: afterUptime " + afterUptime);
        // System.out.println("DEBUG: mean " + mean);
        // System.out.println("DEBUG: test " + delta);
        Assert.assertEquals(1 / (beforeUptime), mean, delta);
        Assert.assertEquals(0.184, timer.getOneMinuteRate(), 0.001);
        Assert.assertEquals(0.196, timer.getFiveMinuteRate(), 0.001);
        Assert.assertEquals(0.198, timer.getFifteenMinuteRate(), 0.001);
    }

    @Test
    @InSequence(2)
    public void testTime() throws Exception {
        Timer timer = registry.timer("testTime");

        double beforeStartTime = System.nanoTime();
        Context context = timer.time();
        double afterStartTime = System.nanoTime();

        double beforeStopTime = System.nanoTime();
        double time = context.stop();
        double afterStopTime = System.nanoTime();
        Thread.sleep(10000);

        double delta = (afterStartTime - beforeStartTime) + (afterStopTime - beforeStopTime);
        Assert.assertEquals(beforeStopTime - beforeStartTime, time, delta);
    }

    @Test
    @InSequence(3)
    public void testTimerRegistry() throws Exception {
        String timerLongName = "test.longData.timer";
        String timerRateName = "testRate";
        String timerTimeName = "testTime";

        SortedMap<String, Timer> timers = registry.getTimers();
        Assert.assertTrue(timers.size() > 0);

        Assert.assertTrue(timers.containsKey(timerLongName));
        Assert.assertTrue(timers.containsKey(timerTimeName));

        Assert.assertEquals(1, timers.get(timerRateName).getCount(), 0);
        Assert.assertEquals(1, registry.timer("testRate").getCount(), 0);
        Assert.assertEquals(480, timers.get(timerLongName).getSnapshot().getValue(0.5), 0);
    }

    @Test
    @InSequence(4)
    public void timesCallableInstances() throws Exception {
        Timer timer = registry.timer("testCallable");
        final String value = timer.time(() -> "one");

        Assert.assertEquals(timer.getCount(), 1);

        Assert.assertEquals(value, "one");
    }

    @Test
    @InSequence(5)
    public void timesRunnableInstances() throws Exception {
        Timer timer = registry.timer("testRunnable");
        final AtomicBoolean called = new AtomicBoolean();
        timer.time(() -> called.set(true));

        Assert.assertEquals(timer.getCount(), 1);

        Assert.assertEquals(called.get(), true);
    }

    @Test
    public void testSnapshotValues() throws Exception {
        Assert.assertArrayEquals(
                "The globalTimer does not contain the expected values: " + Arrays.toString(SAMPLE_LONG_DATA),
                SAMPLE_LONG_DATA, globalTimer.getSnapshot().getValues());
    }

    @Test
    public void testSnapshot75thPercentile() throws Exception {
        Assert.assertEquals(750, globalTimer.getSnapshot().get75thPercentile(), 0);
    }

    @Test
    public void testSnapshot95thPercentile() throws Exception {
        Assert.assertEquals(960, globalTimer.getSnapshot().get95thPercentile(), 0);
    }

    @Test
    public void testSnapshot98thPercentile() throws Exception {
        Assert.assertEquals(980, globalTimer.getSnapshot().get98thPercentile(), 0);
    }

    @Test
    public void testSnapshot99thPercentile() throws Exception {
        Assert.assertEquals(980, globalTimer.getSnapshot().get99thPercentile(), 0);
    }

    @Test
    public void testSnapshot999thPercentile() throws Exception {
        Assert.assertEquals(990, globalTimer.getSnapshot().get999thPercentile(), 0);
    }

    @Test
    public void testSnapshotMax() throws Exception {
        Assert.assertEquals(990.0, globalTimer.getSnapshot().getMax(), 0);
    }

    @Test
    public void testSnapshotMin() throws Exception {
        Assert.assertEquals(0.0, globalTimer.getSnapshot().getMin(), 0);
    }

    @Test
    public void testSnapshotMean() throws Exception {
        Assert.assertEquals(506.3, globalTimer.getSnapshot().getMean(), 0.1);
    }

    @Test
    public void testSnapshotMedian() throws Exception {
        Assert.assertEquals(480, globalTimer.getSnapshot().getMedian(), 0);
    }

    @Test
    public void testSnapshotStdDev() throws Exception {
        Assert.assertEquals(294.3, globalTimer.getSnapshot().getStdDev(), 0.1);
    }

    @Test
    public void testSnapshotSize() throws Exception {
        Assert.assertEquals(200.0, globalTimer.getSnapshot().size(), 0);
    }
}
