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
package org.wildfly.swarm.microprofile_metrics.deployment;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

/**
 * @author hrupp
 */
public class MetricCdiInjectionExtension implements Extension {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

    public MetricCdiInjectionExtension() {

        LOG.warn("+++ Constructor ");

    }

    private void addInterceptorBindings(@Observes BeforeBeanDiscovery bbd, BeanManager manager) {
        LOG.warn("+++ addInterceptorBindings ");
        bbd.addAnnotatedType(manager.createAnnotatedType(Gauge.class));
        bbd.addAnnotatedType(manager.createAnnotatedType(Counted.class));
    }

    public <T> void initializePropertyLoading(final @Observes ProcessInjectionTarget<T> pit) {

        LOG.warn("+++ PIT");
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {

        LOG.warn("+++ AfterBeanDiscovery");
    }

}
