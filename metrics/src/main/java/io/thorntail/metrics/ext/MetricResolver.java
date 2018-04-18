/*
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.thorntail.metrics.ext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;

import javax.enterprise.inject.Vetoed;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

@Vetoed
        /* package-private */ class MetricResolver {


    private MetricName metricName = new SeMetricName(Collections.emptySet());


    <E extends Member & AnnotatedElement> Of<Counted> counted(Class<?> topClass, E element) {
        return resolverOf(topClass, element, Counted.class);
    }

    Of<Gauge> gauge(Class<?> topClass, Method method) {
        return resolverOf(topClass, method, Gauge.class);
    }

    <E extends Member & AnnotatedElement> Of<Metered> metered(Class<?> topClass, E element) {
        return resolverOf(topClass, element, Metered.class);
    }

    <E extends Member & AnnotatedElement> Of<Timed> timed(Class<?> bean, E element) {
        return resolverOf(bean, element, Timed.class);
    }

    private <E extends Member & AnnotatedElement, T extends Annotation> Of<T> resolverOf(Class<?> bean, E element, Class<T> metric) {
        if (element.isAnnotationPresent(metric)) {
            return elementResolverOf(element, metric);
        } else {
            return beanResolverOf(element, metric, bean);
        }
    }

    private <E extends Member & AnnotatedElement, T extends Annotation> Of<T> elementResolverOf(E element, Class<T> metric) {
        T annotation = element.getAnnotation(metric);
        String name = metricName(element, metric, metricName(annotation), isMetricAbsolute(annotation));
        return new DoesHaveMetric<>(annotation, name);
    }

    private <E extends Member & AnnotatedElement, T extends Annotation> Of<T> beanResolverOf(E element, Class<T> metric, Class<?> bean) {
        if (bean.isAnnotationPresent(metric)) {
            T annotation = bean.getAnnotation(metric);
            String name = metricName(bean, element, metric, metricName(annotation), isMetricAbsolute(annotation));
            return new DoesHaveMetric<>(annotation, name);
        } else if (bean.getSuperclass() != null) {
            return beanResolverOf(element, metric, bean.getSuperclass());
        }
        return new DoesNotHaveMetric<>();
    }

    // TODO: should be grouped with the metric name strategy
    private <E extends Member & AnnotatedElement> String metricName(E element, Class<? extends Annotation> type, String name, boolean absolute) {
        String metric = name.isEmpty() ? defaultName(element, type) : metricName.of(name);
        return absolute ? metric : MetricRegistry.name(element.getDeclaringClass(), metric);
    }

    private <E extends Member & AnnotatedElement> String metricName(Class<?> bean, E element, Class<? extends Annotation> type, String name, boolean absolute) {
        String metric = name.isEmpty() ? bean.getSimpleName() : metricName.of(name);
        return absolute ? MetricRegistry.name(metric, defaultName(element, type)) : MetricRegistry.name(bean.getPackage().getName(), metric, defaultName(element, type));
    }

    private <E extends Member & AnnotatedElement> String defaultName(E element, Class<? extends Annotation> type) {

        return memberName(element);
    }

    // While the Member Javadoc states that the getName method should returns
    // the simple name of the underlying member or constructor, the FQN is returned
    // for constructors. See JDK-6294399:
    // http://bugs.java.com/view_bug.do?bug_id=6294399
    private String memberName(Member member) {
        if (member instanceof Constructor) {
            return member.getDeclaringClass().getSimpleName();
        } else {
            return member.getName();
        }
    }

    private String metricName(Annotation annotation) {
        if (Counted.class.isInstance(annotation)) {
            return ((Counted) annotation).name();
        } else if (Gauge.class.isInstance(annotation)) {
            return ((Gauge) annotation).name();
        } else if (Metered.class.isInstance(annotation)) {
            return ((Metered) annotation).name();
        } else if (Timed.class.isInstance(annotation)) {
            return ((Timed) annotation).name();
        } else {
            throw new IllegalArgumentException("Unsupported Metrics forMethod [" + annotation.getClass().getName() + "]");
        }
    }

    private boolean isMetricAbsolute(Annotation annotation) {
/*
        if (extension.getParameters().contains(MetricsParameter.useAbsoluteName)) { TODO
            return true;
        }
*/

        if (Counted.class.isInstance(annotation)) {
            return ((Counted) annotation).absolute();
        } else if (Gauge.class.isInstance(annotation)) {
            return ((Gauge) annotation).absolute();
        } else if (Metered.class.isInstance(annotation)) {
            return ((Metered) annotation).absolute();
        } else if (Timed.class.isInstance(annotation)) {
            return ((Timed) annotation).absolute();
        } else {
            throw new IllegalArgumentException("Unsupported Metrics forMethod [" + annotation.getClass().getName() + "]");
        }
    }

    interface Of<T extends Annotation> {

        boolean isPresent();

        String metricName();

        T metricAnnotation();
    }

    private static final class DoesHaveMetric<T extends Annotation> implements Of<T> {

        private final T annotation;

        private final String name;

        private DoesHaveMetric(T annotation, String name) {
            this.annotation = annotation;
            this.name = name;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public String metricName() {
            return name;
        }

        @Override
        public T metricAnnotation() {
            return annotation;
        }
    }

    @Vetoed
    private static final class DoesNotHaveMetric<T extends Annotation> implements Of<T> {

        private DoesNotHaveMetric() {
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public String metricName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public T metricAnnotation() {
            throw new UnsupportedOperationException();
        }
    }
}
