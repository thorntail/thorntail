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

package io.thorntail.metrics.impl.exporters;

import org.eclipse.microprofile.metrics.MetricUnits;

/**
 * @author hrupp
 */
public class PrometheusUnit {


    private PrometheusUnit() {
    }


    public static String getBaseUnitAsPrometheusString(String unit) {

        String out;
        switch (unit) {

            /* Represents bits. Not defined by SI, but by IEC 60027 */
            case MetricUnits.BITS:
                /* 1000 {@link #BITS} */
            case MetricUnits.KILOBITS:
                /* 1000 {@link #KIBIBITS} */
            case MetricUnits.MEGABITS:
                /* 1000 {@link #MEGABITS} */
            case MetricUnits.GIGABITS:
                /* 1024 {@link #BITS} */
            case MetricUnits.KIBIBITS:
                /* 1024 {@link #KIBIBITS}  */
            case MetricUnits.MEBIBITS:
                /* 1024 {@link #MEBIBITS} */
            case MetricUnits.GIBIBITS:
                /* 8 {@link #BITS} */
            case MetricUnits.BYTES:
                /* 1000 {@link #BYTES} */
            case MetricUnits.KILOBYTES:
                /* 1000 {@link #KILOBYTES} */
            case MetricUnits.MEGABYTES:
                /* 1000 {@link #MEGABYTES} */
            case MetricUnits.GIGABYTES:
                out = "bytes";
                break;

            /* 1/1000 {@link #MICROSECONDS} */
            case MetricUnits.NANOSECONDS:
                /* 1/1000 {@link #MILLISECONDS} */
            case MetricUnits.MICROSECONDS:
                /* 1/1000 {@link #SECONDS} */
            case MetricUnits.MILLISECONDS:
                /* Represents seconds */
            case MetricUnits.SECONDS:
                /* 60 {@link #SECONDS} */
            case MetricUnits.MINUTES:
                /* 60 {@link #MINUTES} */
            case MetricUnits.HOURS:
                /* 24 {@link #HOURS} */
            case MetricUnits.DAYS:
                out = "seconds";
                break;
            default:
                out = unit;
        }
        return out;
    }

    public static Double scaleToBase(String unit, Double value) {


        Double out;

        switch (unit) {

            case MetricUnits.BITS:
                out = value / 8;
                break;
            case MetricUnits.KILOBITS:
                out = value * 1_000 / 8;
                break;
            case MetricUnits.MEGABITS:
                out = value * 1_000_000 / 8;
                break;
            case MetricUnits.GIGABITS:
                out = value * 1_000_000_000 / 8;
                break;
            /* 1024 {@link #BITS} */
            case MetricUnits.KIBIBITS:
                out = value * 128;
                break;
            case MetricUnits.MEBIBITS:
                out = value * 1_024 * 128;
                break;
            case MetricUnits.GIBIBITS:
                out = value * 1_024 * 1_024 * 128;
                break;
            case MetricUnits.BYTES:
                out = value;
                break;
            case MetricUnits.KILOBYTES:
                out = value * 1_000;
                break;
            case MetricUnits.MEGABYTES:
                out = value * 1_000_000;
                break;
            case MetricUnits.GIGABYTES:
                out = value * 1_000_000_000;
                break;
            case MetricUnits.NANOSECONDS:
                out = value / 1_000_000_000;
                break;
            case MetricUnits.MICROSECONDS:
                out = value / 1_000_000;
                break;
            case MetricUnits.MILLISECONDS:
                out = value / 1000;
                break;
            case MetricUnits.SECONDS:
                out = value;
                break;
            case MetricUnits.MINUTES:
                out = value * 60;
                break;
            case MetricUnits.HOURS:
                out = value * 3600;
                break;
            case MetricUnits.DAYS:
                out = value * 24 * 3600;
                break;
            default:
                out = value;
        }

        return out;
    }
}
