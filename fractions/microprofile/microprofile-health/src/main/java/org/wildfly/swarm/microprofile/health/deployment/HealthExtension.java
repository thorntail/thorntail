/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.health.deployment;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.Unmanaged;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.health.api.Monitor;

import io.smallrye.health.SmallRyeHealthReporter;

/**
 * Created by hbraun on 28.06.17.
 */
public class HealthExtension implements Extension {
    private static Logger log = Logger.getLogger(HealthExtension.class);

    private final Monitor monitor;
    private AnnotatedType<?> delegate;
    private Unmanaged.UnmanagedInstance<SmallRyeHealthReporter> reporterInstance;
    private SmallRyeHealthReporter reporter;

    public HealthExtension() {
        try {
            this.monitor = Monitor.lookup();
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup monitor", e);
        }
    }

    public void observeResources(@Observes ProcessAnnotatedType<? extends SmallRyeHealthReporter> event) {

        AnnotatedType<? extends SmallRyeHealthReporter> annotatedType = event.getAnnotatedType();

        if (SmallRyeHealthReporter.class == annotatedType.getJavaClass()) {
            delegate = annotatedType;
        }
    }

    public void afterDeploymentValidation(@Observes final AfterDeploymentValidation abd, BeanManager beanManager) {
        try {
            if (delegate != null) {
                Unmanaged<SmallRyeHealthReporter> unmanagedHealthCheck =
                    new Unmanaged<>(beanManager, SmallRyeHealthReporter.class);
                reporterInstance = unmanagedHealthCheck.newInstance();
                reporter =  reporterInstance.produce().inject().postConstruct().get();
                monitor.registerHealthReporter(reporter);

                // THORN-2195: Use the correct TCCL when health checks are obtained
                // In WildFly, the TCCL should always be set to the top-level deployment CL during extension notification
                monitor.registerContextClassLoader(Thread.currentThread().getContextClassLoader());

                log.info(">> Added health reporter bean " + reporter);
                delegate = null;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to register health reporter bean", e);
        }
    }

    /**
     * Called when the deployment is undeployed.
     *
     * Remove the reporter instance of {@link SmallRyeHealthReporter} from the {@link Monitor}.
     * Handle manually their CDI destroy lifecycle.
     */
    public void beforeShutdown(@Observes final BeforeShutdown bs) {
        monitor.unregisterHealthReporter();
        monitor.unregisterContextClassLoader();
        reporter = null;
        reporterInstance.preDestroy().dispose();
        reporterInstance = null;
    }
}

