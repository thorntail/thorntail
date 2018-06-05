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

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Timed;

@SuppressWarnings("unused")
@Timed
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
/* package-private */ class TimedInterceptor {

    private final Bean<?> bean;

    private final MetricRegistry registry;

    private final MetricResolver resolver;

    @Inject
    private TimedInterceptor(@Intercepted Bean<?> bean, MetricRegistry registry) {
        this.bean = bean;
        this.registry = registry;
        this.resolver = new MetricResolver();
    }

    @AroundConstruct
    private Object timedConstructor(InvocationContext context) throws Exception {
        return timedCallable(context, context.getConstructor());
    }

    @AroundInvoke
    private Object timedMethod(InvocationContext context) throws Exception {
        return timedCallable(context, context.getMethod());
    }

    @AroundTimeout
    private Object timedTimeout(InvocationContext context) throws Exception {
        return timedCallable(context, context.getMethod());
    }

    private <E extends Member & AnnotatedElement> Object timedCallable(InvocationContext context, E element) throws Exception {
        String name = resolver.timed(bean != null ? bean.getBeanClass() : element.getDeclaringClass(), element).metricName();
        Timer timer = (Timer) registry.getMetrics().get(name);
        if (timer == null) {
            throw new IllegalStateException("No timer with name [" + name + "] found in registry [" + registry + "]");
        }

        Timer.Context time = timer.time();
        try {
            return context.proceed();
        } finally {
            time.stop();
        }
    }
}
