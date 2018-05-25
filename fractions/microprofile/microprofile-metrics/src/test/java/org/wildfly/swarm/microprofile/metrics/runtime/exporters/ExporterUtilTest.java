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
package org.wildfly.swarm.microprofile.metrics.runtime.exporters;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 3/23/18
 */
public class ExporterUtilTest {
    @Test
    public void shouldScaleNanosToSecods() {
        Double out = ExporterUtil.convertNanosTo(1.0, MetricUnits.MINUTES);
        Assert.assertEquals(out, 0.0, 1e-10);
    }

    @Test
    public void shouldScaleNanosToMillis() {
        Double out = ExporterUtil.convertNanosTo(1.0, MetricUnits.MILLISECONDS);
        Assert.assertEquals(out, 0.000001, 1e-10);
    }

    @Test
    public void testScaleHoursToSeconds() {
        String foo = MetricUnits.HOURS;
        Double out = ExporterUtil.convertNanosTo(3 * 3600 * 1000_000_000., foo);
        Assert.assertEquals(out, 3., 1e-10);
    }
}