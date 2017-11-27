/*
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
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
 * /
 */
package org.wildfly.swarm.microprofile.health.deployment;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.health.api.Monitor;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.Unmanaged;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by hbraun on 28.06.17.
 */
public class HealthExtension implements Extension {
    private static Logger log = Logger.getLogger(HealthExtension.class);

    private final Monitor monitor;
    private Collection<AnnotatedType> delegates = new ArrayList<>();
    private Collection<HealthCheck> healthChecks = new ArrayList<>();
    private Collection<Unmanaged.UnmanagedInstance<HealthCheck>> healthCheckInstances = new ArrayList<>();

    public HealthExtension() {
        try {
            this.monitor = Monitor.lookup();
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup monitor", e);
        }
    }

    public <T> void observeResources(@Observes @WithAnnotations({Health.class}) ProcessAnnotatedType<T> event) {

        AnnotatedType<T> annotatedType = event.getAnnotatedType();
        Class<T> javaClass = annotatedType.getJavaClass();
        for (Class<?> intf : javaClass.getInterfaces()) {
            if (intf.getName().equals(HealthCheck.class.getName())) {
                log.info(">> Discovered health check procedure " + javaClass);
                delegates.add(annotatedType);
            }
        }
    }

    /**
     * Instantiates <em>unmanaged instances</em> of HealthCheckProcedure and
     * handle manually their CDI creation lifecycle.
     * Add them to the {@link Monitor}.
     */
    private void afterDeploymentValidation(@Observes final AfterDeploymentValidation abd, BeanManager beanManager) {
        try {
            for (AnnotatedType delegate : delegates) {
                Unmanaged<HealthCheck> unmanagedHealthCheck = new Unmanaged<HealthCheck>(beanManager, delegate.getJavaClass());
                Unmanaged.UnmanagedInstance<HealthCheck> healthCheckInstance = unmanagedHealthCheck.newInstance();
                HealthCheck healthCheck =  healthCheckInstance.produce().inject().postConstruct().get();
                healthChecks.add(healthCheck);
                healthCheckInstances.add(healthCheckInstance);

                monitor.registerHealthBean(healthCheck);

                log.info(">> Added health bean impl " + healthCheck);
            }

            // we don't need the references anymore
            delegates.clear();

        } catch (Exception e) {
            throw new RuntimeException("Failed to register health bean", e);
        }
    }

    /**
     * Called when the deployment is undeployed.
     *
     * Remove all the instances of {@link HealthCheck} from the {@link Monitor}.
     * Handle manually their CDI destroy lifecycle.
     */
    public void beforeShutdown(@Observes final BeforeShutdown bs) {
        healthChecks.forEach(healthCheck -> monitor.unregisterHealthBean(healthCheck));
        healthChecks.clear();
        healthCheckInstances.forEach(instance -> instance.preDestroy().dispose());
        healthCheckInstances.clear();
    }
}
