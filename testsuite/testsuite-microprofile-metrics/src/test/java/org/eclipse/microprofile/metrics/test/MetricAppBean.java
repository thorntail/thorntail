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

package org.eclipse.microprofile.metrics.test;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MetricAppBean {

    @Inject
    @Metric
    private Counter redCount;

    @Inject
    @Metric(name = "blue")
    private Counter blueCount;

    @Inject
    @Metric(absolute = true)
    private Counter greenCount;

    @Inject
    @Metric(name = "purple", absolute = true)
    private Counter purpleCount;

    @Inject
    // @RegistryType(type=MetricRegistry.Type.BASE)
    private MetricRegistry metrics;

    public void countMe() {
        Counter counter = metrics.counter("metricTest.test1.count");
        counter.inc();
    }

    @Counted(name = "metricTest.test1.countMeA", monotonic = true, absolute = true)
    public void countMeA() {

    }

    public void gaugeMe() {

        @SuppressWarnings("unchecked")
        Gauge<Long> gauge = metrics.getGauges().get("metricTest.test1.gauge");
        if (gauge == null) {
            gauge = () -> {
                return 19L;
            };
            metrics.register("metricTest.test1.gauge", gauge);
        }

    }

    @org.eclipse.microprofile.metrics.annotation.Gauge(unit = MetricUnits.KIBIBITS)
    public long gaugeMeA() {
        return 1000L;
    }

    public void histogramMe() {

        Histogram histogram = metrics.histogram("metricTest.test1.histogram");

        for (int i = 0; i < 1000; i++) {
            histogram.update(i);
        }

    }

    public void meterMe() {

        Meter meter = metrics.meter("metricTest.test1.meter");
        meter.mark();

    }

    @Metered(absolute = true)
    public void meterMeA() {

    }

    public void timeMe() {

        Timer timer = metrics.timer("metricTest.test1.timer");

        Timer.Context context = timer.time();
        try {
            Thread.sleep((long) (Math.random() * 1000));
        }
        catch (InterruptedException e) {
        }
        finally {
            context.stop();
        }

    }

    @Timed
    public void timeMeA() {

    }

}
