/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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
 *
 */
package org.wildfly.swarm.microprofile.metrics.deployment;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;

/**
 * @author hrupp
 */
public class MetricCdiInjectionExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

    private static final AnnotationLiteral<MetricsBinding> METRICS_BINDING = new AnnotationLiteral<MetricsBinding>() {
    };

    private static final AnnotationLiteral<Default> DEFAULT = new AnnotationLiteral<Default>() {
    };

    private final Map<Bean<?>, AnnotatedMember<?>> metrics = new HashMap<>();

    @Inject
    MetricRegistry registry;

    public MetricCdiInjectionExtension() {
        LOGGER.debug("MetricCdiInjectionExtension");
    }

    private void addInterceptorBindings(@Observes BeforeBeanDiscovery bbd, BeanManager manager) {
        LOGGER.info("MicroProfile: Metrics activated");

        // It seems that fraction deployment module cannot be picked up as a CDI bean archive - see also SWARM-1725
        bbd.addAnnotatedType(manager.createAnnotatedType(MetricProducer.class));
        bbd.addAnnotatedType(manager.createAnnotatedType(MetricNameFactory.class));

        bbd.addAnnotatedType(manager.createAnnotatedType(MeteredInterceptor.class));
        bbd.addAnnotatedType(manager.createAnnotatedType(CountedInterceptor.class));
        bbd.addAnnotatedType(manager.createAnnotatedType(TimedInterceptor.class));
        bbd.addAnnotatedType(manager.createAnnotatedType(MetricsInterceptor.class));
    }

    private <X> void metricsAnnotations(@Observes @WithAnnotations({ Counted.class, Gauge.class, Metered.class, Timed.class }) ProcessAnnotatedType<X> pat) {
        AnnotatedTypeDecorator newPAT = new AnnotatedTypeDecorator<>(pat.getAnnotatedType(), METRICS_BINDING);
        LOGGER.debugf("annotations: %s", newPAT.getAnnotations());
        LOGGER.debugf("methods: %s", newPAT.getMethods());
        pat.setAnnotatedType(newPAT);
    }

    private void metricProducerField(@Observes ProcessProducerField<? extends Metric, ?> ppf) {
        LOGGER.infof("Metrics producer field discovered: %s", ppf.getAnnotatedProducerField());
        metrics.put(ppf.getBean(), ppf.getAnnotatedProducerField());
    }

    private void metricProducerMethod(@Observes ProcessProducerMethod<? extends Metric, ?> ppm) {
        if (!ppm.getBean().getBeanClass().equals(MetricProducer.class)) {
            LOGGER.infof("Metrics producer method discovered: %s", ppm.getAnnotatedProducerMethod());
            metrics.put(ppm.getBean(), ppm.getAnnotatedProducerMethod());
        }
    }

    void registerMetrics(@Observes AfterDeploymentValidation adv, BeanManager manager) {

        // Produce and register custom metrics
        MetricRegistry registry = getReference(manager, MetricRegistry.class);
        MetricName name = getReference(manager, MetricName.class);
        for (Map.Entry<Bean<?>, AnnotatedMember<?>> bean : metrics.entrySet()) {
            if (// skip non @Default beans
            !bean.getKey().getQualifiers().contains(DEFAULT)
                    // skip producer methods with injection point metadata
                    || hasInjectionPointMetadata(bean.getValue())) {
                continue;
            }

            String metricName = name.of(bean.getValue());
            registry.register(metricName, getReference(manager, bean.getValue().getBaseType(), bean.getKey()));
        }

        // Let's clear the collected metric producers
        metrics.clear();
    }

    private static boolean hasInjectionPointMetadata(AnnotatedMember<?> member) {
        if (!(member instanceof AnnotatedMethod)) {
            return false;
        }
        AnnotatedMethod<?> method = (AnnotatedMethod<?>) member;
        for (AnnotatedParameter<?> parameter : method.getParameters()) {
            if (parameter.getBaseType().equals(InjectionPoint.class)) {
                return true;
            }
        }
        return false;
    }

    private static <T> T getReference(BeanManager manager, Class<T> type) {
        return getReference(manager, type, manager.resolve(manager.getBeans(type)));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getReference(BeanManager manager, Type type, Bean<?> bean) {
        return (T) manager.getReference(bean, type, manager.createCreationalContext(bean));
    }
}
