/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.faulttolerance.deployment;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.Unbound;
import org.jboss.weld.manager.BeanManagerImpl;

import io.smallrye.faulttolerance.CommandListener;
import io.smallrye.faulttolerance.config.FaultToleranceOperation;

/**
 * Activates CDI request context when a Hystrix command that wraps a FT operation is executed.
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class RequestContextCommandListener implements CommandListener {

    private final BeanManagerImpl beanManager;

    private final RequestContext requestContext;

    private final ThreadLocal<Boolean> isActivator;

    @Inject
    public RequestContextCommandListener(@Unbound RequestContext requestContext, BeanManagerImpl beanManager) {
        this.requestContext = requestContext;
        this.beanManager = beanManager;
        this.isActivator = new ThreadLocal<>();
    }

    @Override
    public void beforeExecution(FaultToleranceOperation operation) {
        // Note that Hystrix commands are executed on a dedicated thread where a CDI request context should not be active
        // TODO: maybe we should activate the context for any FT operation?
        if (operation.isAsync() && !beanManager.isContextActive(RequestScoped.class)) {
            requestContext.activate();
            isActivator.set(true);
        }
    }

    @Override
    public void afterExecution(FaultToleranceOperation operation) {
        if (Boolean.TRUE.equals(isActivator.get())) {
            try {
                requestContext.invalidate();
                requestContext.deactivate();
            } finally {
                isActivator.remove();
            }
        }
    }

}
