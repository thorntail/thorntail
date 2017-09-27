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
package org.wildfly.swarm.microprofile_metrics.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.SwarmInfo;
import org.wildfly.swarm.microprofile_metrics.MicroprofileMetricsFraction;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;

import javax.inject.Inject;

/**
 * @author hrupp
 */
@DeploymentScoped
public class CdiExtensionInstaller implements DeploymentProcessor {

    private Archive archive;

    @Inject
    MicroprofileMetricsFraction myFraction;

    @Inject
    public CdiExtensionInstaller(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() throws Exception {

        System.err.println("+++ CdiExtensionInstaller ");
/*
        if (archive instanceof WebArchive) {
            System.err.println("Web archive");
        } else if (archive instanceof JARArchive) {
            JARArchive jarArchive = (JARArchive)archive;
            jarArchive.addModule("org.wildfly.swarm.microprofile_metrics");
        } else if (archive instanceof JavaArchive) {
            JavaArchive jarArchive = (JavaArchive)archive;
            jarArchive.addClass(MetricRegistryFactory.class);
        } else {
            System.err.println("Unknown type " + archive.getClass().getName());
//            System.out.println(archive.toString(true));
        }
*/

        System.out.println("   archive " + archive.getName());
        WARArchive war = archive.as(WARArchive.class);
        war.addDependency("org.wildfly.swarm:mp_metrics_cdi_extension:jar:" + SwarmInfo.VERSION);
//        war.addClass(MetricRegistryFactory.class);
//        war.addClass(MetricCdiInjectionExtension.class);

//        war.addAsLibrary("org.eclipse.microprofile.metrics:microprofile-metrics-api:jar");
//        war.addClass(Gauge.class);
//        war.addClass(Counted.class);
//        war.addClass(RegistryType.class);
//        war.addClass(Metric.class);
//        war.addClass(Timed.class);
//        war.addClass(org.eclipse.microprofile.metrics.annotation.Metered.class);
//        war.addClass(org.eclipse.microprofile.metrics.MetricRegistry.class);
//        System.out.println(war.toString(true));
    }
}
