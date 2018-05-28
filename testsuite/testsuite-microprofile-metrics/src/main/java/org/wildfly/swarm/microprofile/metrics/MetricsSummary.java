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
package org.wildfly.swarm.microprofile.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

@ApplicationScoped
public class MetricsSummary {

    @Inject
    @RegistryType(type = MetricRegistry.Type.BASE)
    private MetricRegistry baseMetrics;

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    private MetricRegistry vendorMetrics;

    @Inject
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    private MetricRegistry appMetrics;

    @Inject
    private MetricRegistry defaultMetrics;

    public MetricRegistry getBaseMetrics() {
        return baseMetrics;
    }

    public MetricRegistry getVendorMetrics() {
        return vendorMetrics;
    }

    public MetricRegistry getAppMetrics() {
        return appMetrics;
    }

    public MetricRegistry getDefaultMetrics() {
        return defaultMetrics;
    }

}
