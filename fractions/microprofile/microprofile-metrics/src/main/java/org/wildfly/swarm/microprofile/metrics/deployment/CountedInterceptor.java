/*
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
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
package org.wildfly.swarm.microprofile.metrics.deployment;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import javax.annotation.Priority;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.jboss.logging.Logger;

@SuppressWarnings("unused")
@Counted
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
/* package-private */ class CountedInterceptor {

    private static final Logger LOGGER = Logger.getLogger(CountedInterceptor.class);

    private final Bean<?> bean;

    private final MetricRegistry registry;

    private final MetricResolver resolver;

    @Inject
    private CountedInterceptor(@Intercepted Bean<?> bean, MetricRegistry registry) {
        this.bean = bean;
        this.registry = registry;
        this.resolver = new MetricResolver();
    }

    @AroundConstruct
    private Object countedConstructor(InvocationContext context) throws Exception {
        return countedCallable(context, context.getConstructor());
    }

    @AroundInvoke
    private Object countedMethod(InvocationContext context) throws Exception {
        return countedCallable(context, context.getMethod());
    }

    @AroundTimeout
    private Object countedTimeout(InvocationContext context) throws Exception {
        return countedCallable(context, context.getMethod());
    }

    private <E extends Member & AnnotatedElement> Object countedCallable(InvocationContext context, E element) throws Exception {
        MetricResolver.Of<Counted> counted = resolver.counted(bean != null ? bean.getBeanClass() : element.getDeclaringClass(), element);
        String name = counted.metricName();
        Counter counter = (Counter) registry.getCounters().get(name);
        if (counter == null) {
            throw new IllegalStateException("No counter with name [" + name + "] found in registry [" + registry + "]");
        }
        LOGGER.debugf("Increment counter [metricName: %s]", name);
        counter.inc();
        try {
            return context.proceed();
        } finally {
            if (!counted.metricAnnotation().monotonic()) {
                LOGGER.debugf("Decrement counter [metricName: %s]", name);
                counter.dec();
            }
        }
    }
}
