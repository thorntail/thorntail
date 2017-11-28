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
package org.wildfly.swarm.microprofile.metrics.runtime;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.SwarmInfo;
import org.wildfly.swarm.microprofile.metrics.MicroprofileMetricsFraction;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;

import javax.inject.Inject;

/**
 * @author hrupp
 */
@SuppressWarnings("unused")
@DeploymentScoped
public class CdiExtensionInstaller implements DeploymentProcessor {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

    private Archive archive;

    @Inject
    MicroprofileMetricsFraction myFraction;

    @Inject
    public CdiExtensionInstaller(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() throws Exception {


        if (archive.getName().endsWith(".war")) {
            WARArchive war = archive.as(WARArchive.class);
            war.addDependency("org.wildfly.swarm:metrics-api:jar:" + SwarmInfo.VERSION);
        } else if (archive.getName().endsWith(".jar")) {
            JavaArchive jar = archive.as(JavaArchive.class);
            jar.addPackage("org.wildfly.swarm.microprofile.metrics.cdi");
            jar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        } else {
            LOG.error("Archive " + archive.getName() + " not yet supported");
        }
    }
}
