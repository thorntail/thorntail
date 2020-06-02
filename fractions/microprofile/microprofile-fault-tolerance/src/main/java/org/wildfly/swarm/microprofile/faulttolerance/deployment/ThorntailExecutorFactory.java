/*
 * Copyright 2020 Red Hat, Inc, and individual contributors.
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

import io.smallrye.faulttolerance.DefaultExecutorFactory;

import javax.naming.InitialContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ThorntailExecutorFactory extends DefaultExecutorFactory {
    private final boolean tracingIntegrationRequired = TracingContextProvider.isRequired();

    @Override
    public ExecutorService createCoreExecutor(int size) {
        ExecutorService baseExecutor = super.createCoreExecutor(size);

        if (tracingIntegrationRequired) {
            return MiniConProp.executorService(TracingContextProvider.INSTANCE, baseExecutor);
        } else {
            return baseExecutor;
        }
    }

    @Override
    public ExecutorService createExecutor(int coreSize, int size) {
        ExecutorService baseExecutor = super.createExecutor(coreSize, size);

        if (tracingIntegrationRequired) {
            return MiniConProp.executorService(TracingContextProvider.INSTANCE, baseExecutor);
        } else {
            return baseExecutor;
        }
    }

    @Override
    public ScheduledExecutorService createTimeoutExecutor(int size) {
        ScheduledExecutorService baseExecutor = super.createTimeoutExecutor(size);

        if (tracingIntegrationRequired) {
            return MiniConProp.scheduledExecutorService(TracingContextProvider.INSTANCE, baseExecutor);
        } else {
            return baseExecutor;
        }
    }

    @Override
    protected ThreadFactory threadFactory() {
        try {
            InitialContext initialContext = new InitialContext();
            return (ThreadFactory) initialContext.lookup("java:jboss/ee/concurrency/factory/default");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int priority() {
        return 10;
    }
}
