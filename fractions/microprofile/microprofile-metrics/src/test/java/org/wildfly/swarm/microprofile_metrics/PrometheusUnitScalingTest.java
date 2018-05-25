/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile_metrics;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.junit.Test;
import org.wildfly.swarm.microprofile.metrics.runtime.exporters.PrometheusUnit;

/**
 * @author hrupp
 */
public class PrometheusUnitScalingTest {

    @Test
    public void testScaleToSeconds() {
        String foo = MetricUnits.SECONDS;
        double out = PrometheusUnit.scaleToBase(foo, 3.0);
        assert out == 0.000_000_003 : "Out was " + out;
    }

    @Test
    public void testScaleToSecondsForDays() {
        String foo = MetricUnits.DAYS;
        double out = PrometheusUnit.scaleToBase(foo, 3.0);
        assert out == 0.000_000_003 : "Out was " + out;
    }

    @Test
    public void testScaleMegabyteToByte() {
        String foo = MetricUnits.MEGABYTES;
        double out = PrometheusUnit.scaleToBase(foo, 1.0);
        assert out == 1000 * 1000 : out;
    }

    @Test
    public void testScaleBitsToByte() {
        String foo = MetricUnits.BITS;
        double out = PrometheusUnit.scaleToBase(foo, 13.0);
        assert out == 13.0 / 8.0 : out;
    }

    @Test
    public void testFindBaseUnit1() {
        String foo = MetricUnits.HOURS;
        String out = PrometheusUnit.getBaseUnitAsPrometheusString(foo);
        assert out.equals(MetricUnits.SECONDS);
        String promUnit = PrometheusUnit.getBaseUnitAsPrometheusString(out);
        assert promUnit.equals("seconds");
    }

    @Test
    public void testFindBaseUnit2() {
        String foo = MetricUnits.MILLISECONDS;
        String out = PrometheusUnit.getBaseUnitAsPrometheusString(foo);
        assert out.equals(MetricUnits.SECONDS);
        String promUnit = PrometheusUnit.getBaseUnitAsPrometheusString(out);
        assert promUnit.equals("seconds");
    }

    @Test
    public void testFindBaseUnit3() {
        String foo = MetricUnits.PERCENT;
        String out = PrometheusUnit.getBaseUnitAsPrometheusString(foo);
        assert out.equals(MetricUnits.PERCENT);
    }

    @Test
    public void testFindBaseUnit4() {
        String foo = MetricUnits.NONE;
        String out = PrometheusUnit.getBaseUnitAsPrometheusString(foo);
        assert out.equals(MetricUnits.NONE);
    }
}
