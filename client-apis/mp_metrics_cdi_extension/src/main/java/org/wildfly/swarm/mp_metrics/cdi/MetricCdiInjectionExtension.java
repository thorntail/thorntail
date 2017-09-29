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
package org.wildfly.swarm.mp_metrics.cdi;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Inject;
import javax.interceptor.InterceptorBinding;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author hrupp
 */
@Vetoed
public class MetricCdiInjectionExtension implements Extension {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");

    private static final AnnotationLiteral<InterceptorBinding> INTERCEPTOR_BINDING = new AnnotationLiteral<InterceptorBinding>() { };
    private static final AnnotationLiteral<Nonbinding> NON_BINDING = new AnnotationLiteral<Nonbinding>() { };
    private static final AnnotationLiteral<MetricsBinding> METRICS_BINDING = new AnnotationLiteral<MetricsBinding>() { };

    private final Map<Bean<?>, AnnotatedMember<?>> metrics = new HashMap<>();

    @Inject
    MetricRegistry registry;

    public MetricCdiInjectionExtension() {

        LOG.warn("+++ Constructor ");

        try {
            InitialContext context = new InitialContext();
            Object o =   context.lookup("jboss/swarm/mp_metrics");
            System.out.println("Got a " + o.getClass().getName() + " with CL " + o.getClass().getClassLoader().toString());
            System.out.println("    Our CL : " + this.getClass().getClassLoader().toString());
        } catch (NamingException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }


    }

    private void addInterceptorBindings(@Observes BeforeBeanDiscovery bbd, BeanManager manager) {
        declareAsInterceptorBinding(Counted.class,manager,bbd);
        declareAsInterceptorBinding(Gauge.class,manager,bbd);
        declareAsInterceptorBinding(Timed.class,manager,bbd);
        declareAsInterceptorBinding(Metered.class,manager,bbd);
    }

    private <X> void metricsAnnotations(@Observes @WithAnnotations({ Counted.class,  Gauge.class, Metered.class, Timed.class}) ProcessAnnotatedType<X> pat) {
        pat.setAnnotatedType(new AnnotatedTypeDecorator<>(pat.getAnnotatedType(), METRICS_BINDING));
    }

    private void metricProducerField(@Observes ProcessProducerField<? extends Metric, ?> ppf) {
        metrics.put(ppf.getBean(), ppf.getAnnotatedProducerField());
    }

/*
    private void metricProducerMethod(@Observes ProcessProducerMethod<? extends Metric, ?> ppm) {
        // Skip the Metrics CDI alternatives
        if (!ppm.getBean().getBeanClass().equals(MetricProducer.class))
            metrics.put(ppm.getBean(), ppm.getAnnotatedProducerMethod());
    }
*/


    public <T> void initializePropertyLoading(final @Observes ProcessInjectionTarget<T> pit) {

//        LOG.warn("+++ PIT: " + pit.getInjectionTarget().toString());
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {

//        LOG.warn("+++ AfterBeanDiscovery");
    }



    private static <T extends Annotation> void declareAsInterceptorBinding(Class<T> annotation, BeanManager manager, BeforeBeanDiscovery bbd) {
          AnnotatedType<T> annotated = manager.createAnnotatedType(annotation);
          Set<AnnotatedMethod<? super T>> methods = new HashSet<>();
          for (AnnotatedMethod<? super T> method : annotated.getMethods()) {
              methods.add(new AnnotatedMethodDecorator<>(method, NON_BINDING));
          }

          bbd.addInterceptorBinding(new AnnotatedTypeDecorator<>(annotated, INTERCEPTOR_BINDING, methods));
      }
}

