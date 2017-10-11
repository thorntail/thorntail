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

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author hrupp
 */
@ApplicationScoped
@Named("My Factory")
public class AMetricRegistryFactory {

    private static final Map<MetricRegistry.Type, MetricRegistry> registries = new HashMap<>();
    private static final String DOT = ".";

    private AMetricRegistryFactory() { /* Singleton */ }

    @Default
    @Produces
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    public static MetricRegistry getApplicationRegistry() {
        return get(MetricRegistry.Type.APPLICATION);
    }


    @Produces
    public static Counter getCounter(InjectionPoint ip) {
        String beanName = ip.getBean() != null ? ip.getBean().getBeanClass().getName() : ip.getMember().getDeclaringClass().getName();
        Set<Annotation> annotations = ip.getAnnotated().getAnnotations();

        String fieldName = ip.getMember().getName();
        String name = beanName + DOT + fieldName;

        for (Annotation a : annotations) {
            if (a.annotationType().equals(Metric.class)) {
                Metric m = (Metric) a;
                if (!m.name().isEmpty()) {
                    fieldName = m.name();
                }
                if (!m.absolute()) {
                    name = beanName + DOT + fieldName;
                } else {
                    name = fieldName;
                }
                Metadata metadata = getMetadata(name,m,MetricType.COUNTER);
                return getApplicationRegistry().counter(metadata);
            }
        }

        return getApplicationRegistry().counter(name);
    }


    @Produces
    public static Histogram getHistogram(InjectionPoint ip) {


        String beanName = ip.getBean() != null ? ip.getBean().getBeanClass().getName() : ip.getMember().getDeclaringClass().getName();
        Set<Annotation> annotations = ip.getAnnotated().getAnnotations();

        String fieldName = ip.getMember().getName();
        String name = beanName + DOT + fieldName;
        for (Annotation a : annotations) {
            if (a.annotationType().equals(Metric.class)) {
                Metadata metadata = getMetadata(name, (Metric) a, MetricType.HISTOGRAM);

                return getApplicationRegistry().histogram(metadata);
            }
        }
        return getApplicationRegistry().histogram(name);
    }

    @Produces
    public static Meter getMeter(InjectionPoint ip) {

        String beanName = ip.getBean() != null ? ip.getBean().getBeanClass().getName() : ip.getMember().getDeclaringClass().getName();
        Set<Annotation> annotations = ip.getAnnotated().getAnnotations();

        String fieldName = ip.getMember().getName();
        String name = beanName + DOT + fieldName;
        for (Annotation a : annotations) {
            if (a.annotationType().equals(Metric.class)) {
                Metadata metadata = getMetadata(name, (Metric) a, MetricType.METERED);

                return getApplicationRegistry().meter(metadata);
            }
        }
        return getApplicationRegistry().meter(name);
    }

    @Produces
    public static Timer getTimer(InjectionPoint ip) {


        String beanName = ip.getBean() != null ? ip.getBean().getBeanClass().getName() : ip.getMember().getDeclaringClass().getName();
        Set<Annotation> annotations = ip.getAnnotated().getAnnotations();

        String fieldName = ip.getMember().getName();
        String name = beanName + DOT + fieldName;
        for (Annotation a : annotations) {
            if (a.annotationType().equals(Metric.class)) {
                Metadata metadata = getMetadata(name, (Metric) a, MetricType.TIMER);

                return getApplicationRegistry().timer(metadata);
            }
        }
        return getApplicationRegistry().timer(name);

    }

    private static Metadata getMetadata(String name, Metric a, MetricType type) {
        Metric m = a;

        Metadata metadata = new Metadata(name, type);
        if (!m.unit().isEmpty()) {
            metadata.setUnit(m.unit());
        }
        if (!m.description().isEmpty()) {
            metadata.setDescription(m.description());
        }
        if (!m.displayName().isEmpty()) {
            metadata.setDisplayName(m.displayName());
        }
        if (m.tags().length > 0) {
            for (String tag : m.tags()) {
                metadata.addTags(tag);
            }
        }
        return metadata;
    }
/*
  @Produces
  @RegistryType(type = MetricRegistry.Type.BASE)
  public static MetricRegistry getBaseRegistry() {
    return get(MetricRegistry.Type.BASE);
  }

  @Produces
  @RegistryType(type = MetricRegistry.Type.VENDOR)
  public static MetricRegistry getVendorRegistry() {
    return get(MetricRegistry.Type.VENDOR);
  }
*/

    public static MetricRegistry get(MetricRegistry.Type type) {

        synchronized (registries) {
            if (registries.get(type) == null) {

                try {
                    InitialContext context = new InitialContext();
                    Object o = context.lookup("jboss/swarm/mp_metrics");

                    RegistryFactory factory = (RegistryFactory) o;
                    MetricRegistry result = factory.get(type);
                    registries.put(type, result);
                } catch (NamingException e) {
                    e.printStackTrace();  // TODO: Customise this generated block
                }
            }
        }

        return registries.get(type);
    }
}
