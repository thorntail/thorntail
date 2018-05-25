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

/**
 * @author hrupp, Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 */
public class ExporterUtil {

    public static final long NANOS_PER_MICROSECOND = 1_000;
    public static final long NANOS_PER_MILLI = 1_000_000;
    public static final long NANOS_PER_SECOND = 1_000_000_000;
    public static final long NANOS_PER_MINUTE = 60 * 1_000_000_000L;
    public static final long NANOS_PER_HOUR = 3600 * 1_000_000_000L;
    public static final long NANOS_PER_DAY = 24 * 3600 * 1_000_000_000L;

    private ExporterUtil() {
    }

    public static Double convertNanosTo(Double value, String unit) {

        Double out;

        switch (unit) {
            case MetricUnits.NANOSECONDS:
                out = value;
                break;
            case MetricUnits.MICROSECONDS:
                out = value / NANOS_PER_MICROSECOND;
                break;
            case MetricUnits.MILLISECONDS:
                out = value / NANOS_PER_MILLI;
                break;
            case MetricUnits.SECONDS:
                out = value / NANOS_PER_SECOND;
                break;
            case MetricUnits.MINUTES:
                out = value / NANOS_PER_MINUTE;
                break;
            case MetricUnits.HOURS:
                out = value / NANOS_PER_HOUR;
                break;
            case MetricUnits.DAYS:
                out = value / NANOS_PER_DAY;
                break;
            default:
                out = value;
        }
        return out;
    }
}
